package pl.backend.weddinggallery.upload.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.Iterator;
import java.util.concurrent.Semaphore;
import javax.imageio.*;
import javax.imageio.stream.ImageInputStream;
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
	@Value("${app.upload.max-image-pixels:25000000}")
	private long maxImagePixels = 25_000_000L;
	private final Semaphore imageDecodeSlots = new Semaphore(2, true);
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
		byte[] header = new byte[32];
		int length;
		try (InputStream input = file.getInputStream()) {
			length = input.read(header);
		} catch (IOException ex) {
			throw new AppException(UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		}
		if (!matches(expected.detectedContentType(), header, length, file))
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
	private boolean matches(String type, byte[] h, int n, MultipartFile file) {
		return switch (type) {
			case "image/jpeg" -> n >= 4 && u(h[0]) == 0xff && u(h[1]) == 0xd8 && u(h[2]) == 0xff
					&& endsWith(file, new byte[]{(byte) 0xff, (byte) 0xd9}) && readableImage(file, "JPEG");
			case "image/png" -> n >= 8 && u(h[0]) == 0x89 && h[1] == 'P' && h[2] == 'N' && h[3] == 'G'
					&& u(h[4]) == 0x0d && u(h[5]) == 0x0a && u(h[6]) == 0x1a && u(h[7]) == 0x0a
					&& endsWith(file, new byte[]{0, 0, 0, 0, 'I', 'E', 'N', 'D', (byte) 0xae, 0x42, 0x60, (byte) 0x82})
					&& readableImage(file, "PNG");
			case "image/webp" -> validWebp(file, h, n);
			case "video/mp4" -> validMp4(file, h, n);
			default -> false;
		};
	}
	private boolean endsWith(MultipartFile file, byte[] suffix) {
		if (file.getSize() < suffix.length)
			return false;
		try (InputStream input = file.getInputStream()) {
			input.skipNBytes(file.getSize() - suffix.length);
			return java.util.Arrays.equals(input.readNBytes(suffix.length), suffix);
		} catch (IOException ex) {
			return false;
		}
	}
	private boolean readableImage(MultipartFile file, String format) {
		try (ImageInputStream input = ImageIO.createImageInputStream(file.getInputStream())) {
			if (input == null)
				return false;
			Iterator<ImageReader> readers = ImageIO.getImageReaders(input);
			if (!readers.hasNext())
				return false;
			ImageReader reader = readers.next();
			try {
				reader.setInput(input, true, true);
				int width = reader.getWidth(0);
				int height = reader.getHeight(0);
				if (!reader.getFormatName().equalsIgnoreCase(format) || width <= 0 || height <= 0
						|| (long) width * height > maxImagePixels)
					return false;
				if (!imageDecodeSlots.tryAcquire())
					throw new AppException(UploadErrorCode.UPLOAD_VALIDATION_BUSY);
				try {
					var image = reader.read(0);
					return image != null && image.getWidth() == width && image.getHeight() == height;
				} finally {
					imageDecodeSlots.release();
				}
			} finally {
				reader.dispose();
			}
		} catch (AppException ex) {
			throw ex;
		} catch (IOException | RuntimeException ex) {
			return false;
		}
	}
	private boolean validWebp(MultipartFile file, byte[] header, int length) {
		if (length < 21 || !ascii(header, 0, "RIFF") || !ascii(header, 8, "WEBP")
				|| littleEndianInt(header, 4) + 8L != file.getSize())
			return false;
		if (ascii(header, 12, "VP8L"))
			return u(header[20]) == 0x2f;
		if (ascii(header, 12, "VP8 "))
			return length >= 26 && u(header[23]) == 0x9d && u(header[24]) == 0x01 && u(header[25]) == 0x2a;
		if (ascii(header, 12, "VP8X"))
			return length >= 30 && littleEndianInt(header, 16) == 10;
		return false;
	}
	private boolean validMp4(MultipartFile file, byte[] header, int length) {
		if (length < 12 || !ascii(header, 4, "ftyp") || bigEndianInt(header, 0) < 16)
			return false;
		boolean mediaData = false;
		boolean movie = false;
		boolean compatibleBrand = false;
		try (InputStream input = file.getInputStream()) {
			long remaining = file.getSize();
			byte[] box = new byte[8];
			while (remaining >= 8) {
				if (input.readNBytes(box, 0, 8) != 8)
					return false;
				long size = Integer.toUnsignedLong(bigEndianInt(box, 0));
				if (size < 8 || size > remaining)
					return false;
				if (ascii(box, 4, "ftyp")) {
					byte[] brand = input.readNBytes(4);
					compatibleBrand |= brand.length == 4 && (ascii(brand, 0, "isom") || ascii(brand, 0, "iso2")
							|| ascii(brand, 0, "mp41") || ascii(brand, 0, "mp42") || ascii(brand, 0, "avc1"));
					input.skipNBytes(size - 12);
				} else {
					mediaData |= ascii(box, 4, "mdat") && size > 8;
					movie |= ascii(box, 4, "moov") && size > 8;
					input.skipNBytes(size - 8);
				}
				remaining -= size;
			}
			return remaining == 0 && compatibleBrand && mediaData && movie;
		} catch (IOException ex) {
			return false;
		}
	}
	private int littleEndianInt(byte[] value, int offset) {
		return u(value[offset]) | u(value[offset + 1]) << 8 | u(value[offset + 2]) << 16 | u(value[offset + 3]) << 24;
	}
	private int bigEndianInt(byte[] value, int offset) {
		return u(value[offset]) << 24 | u(value[offset + 1]) << 16 | u(value[offset + 2]) << 8 | u(value[offset + 3]);
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
