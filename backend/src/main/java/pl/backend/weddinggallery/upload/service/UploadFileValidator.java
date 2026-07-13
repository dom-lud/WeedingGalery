package pl.backend.weddinggallery.upload.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.media.model.MediaType;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;

@Component
public class UploadFileValidator {
	private final long maxImageBytes;
	private final long maxVideoBytes;
	public UploadFileValidator(@Value("${app.upload.max-image-bytes:26214400}") long maxImageBytes,
			@Value("${app.upload.max-video-bytes:524288000}") long maxVideoBytes) {
		this.maxImageBytes = maxImageBytes;
		this.maxVideoBytes = maxVideoBytes;
	}
	public DetectedFile validate(String expectedName, String declaredType, long expectedSize, MultipartFile file) {
		if (file.isEmpty())
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		if (file.getSize() != expectedSize)
			throw new AppException(UploadErrorCode.UPLOAD_SIZE_MISMATCH);
		String actualName = basename(file.getOriginalFilename());
		if (!actualName.equals(expectedName))
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		String extension = extension(expectedName);
		DetectedFile expected = expected(extension, declaredType);
		if (file.getSize() > expected.maxBytes())
			throw new AppException(UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
		byte[] header = new byte[16];
		int length;
		try (InputStream input = file.getInputStream()) {
			length = input.read(header);
		} catch (IOException ex) {
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		}
		if (!matches(expected.detectedContentType(), header, length))
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		return expected;
	}
	public DetectedFile validateDeclaration(String fileName, String declaredType, long size) {
		DetectedFile detected = expected(extension(fileName), declaredType);
		if (size > detected.maxBytes())
			throw new AppException(UploadErrorCode.UPLOAD_FILE_TOO_LARGE);
		return detected;
	}
	private DetectedFile expected(String extension, String contentType) {
		String normalized = contentType.toLowerCase(Locale.ROOT);
		return switch (extension) {
			case "jpg", "jpeg" -> require(normalized, "image/jpeg", MediaType.IMAGE, maxImageBytes);
			case "png" -> require(normalized, "image/png", MediaType.IMAGE, maxImageBytes);
			case "webp" -> require(normalized, "image/webp", MediaType.IMAGE, maxImageBytes);
			case "mp4" -> require(normalized, "video/mp4", MediaType.VIDEO, maxVideoBytes);
			default -> throw new AppException(UploadErrorCode.UPLOAD_TYPE_NOT_ALLOWED);
		};
	}
	private DetectedFile require(String actual, String required, MediaType type, long max) {
		if (!actual.equals(required))
			throw new AppException(UploadErrorCode.UPLOAD_TYPE_NOT_ALLOWED);
		return new DetectedFile(type, required, max);
	}
	private boolean matches(String type, byte[] h, int n) {
		return switch (type) {
			case "image/jpeg" -> n >= 4 && u(h[0]) == 0xff && u(h[1]) == 0xd8 && u(h[2]) == 0xff;
			case "image/png" -> n >= 8 && u(h[0]) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
					&& u(h[4]) == 0x0d && u(h[5]) == 0x0a && u(h[6]) == 0x1a && u(h[7]) == 0x0a;
			case "image/webp" -> n >= 12 && ascii(h, 0, "RIFF") && ascii(h, 8, "WEBP");
			case "video/mp4" -> n >= 12 && ascii(h, 4, "ftyp");
			default -> false;
		};
	}
	private int u(byte value) {
		return value & 0xff;
	}
	private boolean ascii(byte[] h, int offset, String text) {
		for (int i = 0; i < text.length(); i++)
			if (h[offset + i] != text.charAt(i))
				return false;
		return true;
	}
	private String extension(String name) {
		int dot = name.lastIndexOf('.');
		return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
	}
	private String basename(String name) {
		if (name == null || name.indexOf('\0') >= 0)
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		String clean = name.replace('\\', '/');
		clean = clean.substring(clean.lastIndexOf('/') + 1);
		if (clean.isBlank() || clean.length() > 255)
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		return clean;
	}
	public record DetectedFile(MediaType mediaType, String detectedContentType, long maxBytes) {
	}
}
