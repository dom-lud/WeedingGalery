package pl.backend.weddinggallery.storage;

import java.io.InputStream;

public interface StorageService {
	StoredObject save(String objectKey, InputStream input, long maxBytes);
	InputStream open(String objectKey);
	void delete(String objectKey);
	boolean exists(String objectKey);
}
