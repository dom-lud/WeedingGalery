package pl.backend.weddinggallery.storage;

import static org.assertj.core.api.Assertions.*;

import java.io.ByteArrayInputStream;
import java.nio.file.*;
import org.junit.jupiter.api.Test;
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
		for (String key : new String[]{"../escape", "events\\escape", "/absolute", "events//file", "events/./file"}) {
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
	void refusesSymlinkedStorageSegmentsWhenPlatformSupportsSymlinks() throws Exception {
		Path outsideFile = Files.write(outside.resolve("file"), new byte[]{1});
		Path link = root.resolve("events");
		try {
			Files.createSymbolicLink(link, outside);
		} catch (UnsupportedOperationException | FileSystemException ex) {
			return;
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
