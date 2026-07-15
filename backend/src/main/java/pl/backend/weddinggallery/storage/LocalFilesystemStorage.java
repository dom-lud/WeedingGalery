package pl.backend.weddinggallery.storage;

import jakarta.annotation.PostConstruct;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.PosixFilePermissions;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.UUID;
import java.time.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;

@Component
public class LocalFilesystemStorage implements StorageService {
	private final Path root;
	public LocalFilesystemStorage(@Value("${app.storage.local.root:./build/media}") String root) {
		this.root = Path.of(root).toAbsolutePath().normalize();
	}
	@PostConstruct
	void initialize() {
		try {
			Files.createDirectories(root);
			if (Files.isSymbolicLink(root))
				throw new IOException("Storage root cannot be a symbolic link");
			restrict(root, "rwx------");
		} catch (IOException ex) {
			throw new IllegalStateException("Storage root cannot be initialized", ex);
		}
	}
	@Override
	public StoredObject save(String objectKey, InputStream input, long maxBytes) {
		Path target = resolve(objectKey);
		Path temporary = target.resolveSibling(target.getFileName() + "." + UUID.randomUUID() + ".tmp");
		try {
			Files.createDirectories(target.getParent());
			assertNoSymlinks(target.getParent());
			MessageDigest digest = MessageDigest.getInstance("SHA-256");
			long size = 0;
			try (InputStream source = input;
					OutputStream output = Files.newOutputStream(temporary, StandardOpenOption.CREATE_NEW,
							StandardOpenOption.WRITE)) {
				byte[] buffer = new byte[64 * 1024];
				int read;
				while ((read = source.read(buffer)) >= 0) {
					if (read == 0)
						continue;
					size += read;
					if (size > maxBytes)
						throw new AppException(UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
					digest.update(buffer, 0, read);
					output.write(buffer, 0, read);
				}
			}
			restrict(temporary, "rw-------");
			Files.move(temporary, target, StandardCopyOption.ATOMIC_MOVE);
			return new StoredObject(size, HexFormat.of().formatHex(digest.digest()));
		} catch (AppException ex) {
			deleteQuietly(temporary);
			throw ex;
		} catch (Exception ex) {
			deleteQuietly(temporary);
			throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		}
	}
	@Override
	public InputStream open(String objectKey) {
		try {
			Path path = resolve(objectKey);
			assertNoSymlinks(path);
			return Files.newInputStream(path, LinkOption.NOFOLLOW_LINKS);
		} catch (IOException ex) {
			throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		}
	}
	@Override
	public void delete(String objectKey) {
		try {
			Files.deleteIfExists(resolve(objectKey));
		} catch (IOException ex) {
			throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		}
	}
	@Override
	public boolean exists(String objectKey) {
		Path path = resolve(objectKey);
		return Files.isRegularFile(path, LinkOption.NOFOLLOW_LINKS);
	}
	@Scheduled(fixedDelayString = "${app.upload.temp-cleanup-interval-ms:3600000}", initialDelayString = "${app.upload.temp-cleanup-initial-delay-ms:300000}")
	void cleanupStaleTemporaryFiles() {
		Instant cutoff = Instant.now().minus(Duration.ofHours(1));
		try (var paths = Files.walk(root)) {
			paths.filter(path -> path.getFileName().toString().endsWith(".tmp"))
					.filter(path -> !Files.isSymbolicLink(path)).filter(path -> {
						try {
							return Files.getLastModifiedTime(path, LinkOption.NOFOLLOW_LINKS).toInstant()
									.isBefore(cutoff);
						} catch (IOException ex) {
							return false;
						}
					}).forEach(this::deleteQuietly);
		} catch (IOException ignored) {
		}
	}
	private Path resolve(String key) {
		if (key == null || key.isBlank() || key.indexOf('\0') >= 0 || key.contains("\\") || key.startsWith("/")
				|| key.split("/").length == 0)
			throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		for (String part : key.split("/"))
			if (part.isBlank() || part.equals(".") || part.equals(".."))
				throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		Path resolved = root.resolve(key).normalize();
		if (!resolved.startsWith(root))
			throw new AppException(UploadErrorCode.STORAGE_WRITE_FAILED);
		return resolved;
	}
	private void assertNoSymlinks(Path path) throws IOException {
		Path current = root;
		Path relative = root.relativize(path.toAbsolutePath().normalize());
		for (Path part : relative) {
			current = current.resolve(part);
			if (Files.exists(current, LinkOption.NOFOLLOW_LINKS) && Files.isSymbolicLink(current))
				throw new IOException("Symbolic links are forbidden");
		}
	}
	private void deleteQuietly(Path path) {
		try {
			Files.deleteIfExists(path);
		} catch (IOException ignored) {
		}
	}
	private void restrict(Path path, String permissions) {
		try {
			Files.setPosixFilePermissions(path, PosixFilePermissions.fromString(permissions));
		} catch (UnsupportedOperationException | IOException ignored) {
		}
	}
}
