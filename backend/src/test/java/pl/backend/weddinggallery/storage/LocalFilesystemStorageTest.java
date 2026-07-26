package pl.backend.weddinggallery.storage;

import static org.assertj.core.api.Assertions.*;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.FileTime;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.io.TempDir;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;

class LocalFilesystemStorageTest {
	@TempDir
	Path root;
	@TempDir
	Path outside;

	@Test
	void storesUnderServerKeyWithoutOverwriteAndDeletesIdempotently() throws Exception {
		LocalFilesystemStorage storage = storage();
		String key = "events/e/galleries/g/media/m/original";
		StoredObject stored = storage.save(key, new ByteArrayInputStream("safe".getBytes()), 4);
		assertThat(stored.size()).isEqualTo(4);
		assertThat(stored.checksumSha256()).hasSize(64);
		assertThat(storage.exists(key)).isTrue();
		assertThat(storage.open(key).readAllBytes()).isEqualTo("safe".getBytes());
		assertThatThrownBy(() -> storage.save(key, new ByteArrayInputStream("again".getBytes()), 5))
				.isInstanceOf(AppException.class);
		assertThat(storage.open(key).readAllBytes()).isEqualTo("safe".getBytes());
		storage.delete(key);
		storage.delete(key);
		assertThat(storage.exists(key)).isFalse();
	}

	@Test
	void rejectsTraversalBackslashesAbsoluteKeysAndOversizedStreamsWithoutLeavingTempFiles() throws Exception {
		LocalFilesystemStorage storage = storage();
		for (String key : new String[]{null, "", " ", "bad\0key", "../escape", "events/..", "events\\escape",
				"/absolute", "events//file", "events/./file"}) {
			assertThatThrownBy(() -> storage.save(key, new ByteArrayInputStream(new byte[]{1}), 1)).as(key)
					.isInstanceOf(AppException.class);
		}
		assertThatThrownBy(() -> storage.save("events/e/file", new ByteArrayInputStream(new byte[]{1, 2}), 1))
				.isInstanceOfSatisfying(AppException.class,
						ex -> assertThat(ex.getErrorCode()).isEqualTo(UploadErrorCode.UPLOAD_FILE_TOO_LARGE));
		try (var paths = Files.walk(root)) {
			assertThat(paths.filter(path -> path.toString().endsWith(".tmp"))).isEmpty();
		}
	}

	@Test
	void mapsMissingObjectsAndBrokenStreamsToStableStorageErrors() {
		LocalFilesystemStorage storage = storage();
		assertThatThrownBy(() -> storage.open("missing/file")).isInstanceOfSatisfying(AppException.class,
				error -> assertThat(error.getErrorCode()).isEqualTo(UploadErrorCode.STORAGE_WRITE_FAILED));
		assertThatCode(() -> storage.delete("missing/file")).doesNotThrowAnyException();
		assertThat(storage.exists("missing/file")).isFalse();

		InputStream broken = new InputStream() {
			private boolean returnedZero;
			@Override
			public int read() throws IOException {
				throw new IOException("broken source");
			}
			@Override
			public int read(byte[] bytes) throws IOException {
				if (!returnedZero) {
					returnedZero = true;
					return 0;
				}
				throw new IOException("broken source");
			}
		};
		assertThatThrownBy(() -> storage.save("events/e/broken", broken, 1)).isInstanceOfSatisfying(AppException.class,
				error -> assertThat(error.getErrorCode()).isEqualTo(UploadErrorCode.STORAGE_WRITE_FAILED));
	}

	@Test
	void cleanupDeletesOnlyStaleNonSymlinkTemporaryFiles() throws Exception {
		LocalFilesystemStorage storage = storage();
		Path directory = Files.createDirectories(root.resolve("nested"));
		Path stale = Files.write(directory.resolve("stale.tmp"), new byte[]{1});
		Path fresh = Files.write(directory.resolve("fresh.tmp"), new byte[]{1});
		Path permanent = Files.write(directory.resolve("keep.jpg"), new byte[]{1});
		Path symlink = directory.resolve("link.tmp");
		boolean symlinkSupported = true;
		try {
			Files.createSymbolicLink(symlink, permanent);
		} catch (UnsupportedOperationException | IOException | SecurityException ex) {
			symlinkSupported = false;
		}
		Assumptions.assumeTrue(symlinkSupported, "Symbolic links are not supported by this filesystem");
		Files.setLastModifiedTime(stale, FileTime.from(Instant.now().minusSeconds(7200)));

		storage.cleanupStaleTemporaryFiles();

		assertThat(stale).doesNotExist();
		assertThat(fresh).exists();
		assertThat(permanent).exists();
		assertThat(symlink).exists();
		assertThat(Files.isSymbolicLink(symlink)).isTrue();
	}

	@Test
	void initializationFailsWhenConfiguredRootIsARegularFile() throws Exception {
		Path fileRoot = Files.write(root.resolve("not-a-directory"), new byte[]{1});
		LocalFilesystemStorage storage = new LocalFilesystemStorage(fileRoot.toString());
		assertThatThrownBy(storage::initialize).isInstanceOf(IllegalStateException.class)
				.hasMessage("Storage root cannot be initialized");
	}

	@Test
	void refusesSymlinkedStorageSegmentsWhenPlatformSupportsSymlinks() throws Exception {
		Path outsideFile = Files.write(outside.resolve("file"), new byte[]{1});
		Path link = root.resolve("events");
		try {
			Files.createSymbolicLink(link, outside);
		} catch (UnsupportedOperationException | FileSystemException | SecurityException ex) {
			Assumptions.assumeTrue(false, "Symbolic links are not supported by this filesystem");
		}
		LocalFilesystemStorage storage = storage();
		assertThatThrownBy(() -> storage.save("events/e/file", new ByteArrayInputStream(new byte[]{1}), 1))
				.isInstanceOf(AppException.class);
		assertThat(storage.exists("events/file")).isFalse();
		assertThatThrownBy(() -> storage.delete("events/file")).isInstanceOf(AppException.class);
		assertThat(outsideFile).exists();
		assertThat(outside.resolve("e")).doesNotExist();
	}

	private LocalFilesystemStorage storage() {
		LocalFilesystemStorage storage = new LocalFilesystemStorage(root.toString());
		storage.initialize();
		return storage;
	}
}
