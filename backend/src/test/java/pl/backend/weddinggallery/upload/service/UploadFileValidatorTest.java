package pl.backend.weddinggallery.upload.service;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import javax.imageio.ImageIO;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.upload.exception.UploadErrorCode;

class UploadFileValidatorTest {
	private final UploadFileValidator validator = new UploadFileValidator(2048, 2048);

	@Test
	void acceptsOnlyMatchingExtensionMimeMagicAndManifestSize() {
		byte[] jpeg = jpeg();
		var result = validator.validate("photo.jpg", "image/jpeg", jpeg.length,
				new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpeg));
		assertThat(result.detectedContentType()).isEqualTo("image/jpeg");
	}

	@Test
	void rejectsSignatureOnlyPayloads() {
		byte[] fakeJpeg = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0};
		assertCode(new MockMultipartFile("file", "photo.jpg", "image/jpeg", fakeJpeg), "photo.jpg", "image/jpeg",
				fakeJpeg.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		byte[] fakeMp4 = {0, 0, 0, 12, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
		assertCode(new MockMultipartFile("file", "video.mp4", "video/mp4", fakeMp4), "video.mp4", "video/mp4",
				fakeMp4.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
	}

	private byte[] jpeg() {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), "jpg", output);
			return output.toByteArray();
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	@Test
	void rejectsSpoofedEmptyMismatchedAndDisallowedFiles() {
		assertCode(new MockMultipartFile("file", "photo.jpg", "image/jpeg", "text".getBytes()), "photo.jpg",
				"image/jpeg", 4, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		assertCode(
				new MockMultipartFile("file", "photo.jpg", "text/plain",
						new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0}),
				"photo.jpg", "text/plain", 4, UploadErrorCode.UPLOAD_TYPE_NOT_ALLOWED);
		assertCode(new MockMultipartFile("file", "image.svg", "image/svg+xml", "<svg/>".getBytes()), "image.svg",
				"image/svg+xml", 6, UploadErrorCode.UPLOAD_TYPE_NOT_ALLOWED);
		assertCode(new MockMultipartFile("file", "photo.png", "image/png", new byte[0]), "photo.png", "image/png", 0,
				UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		assertCode(
				new MockMultipartFile("file", "photo.jpg", "image/jpeg",
						new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff, 0}),
				"photo.jpg", "image/jpeg", 5, UploadErrorCode.UPLOAD_SIZE_MISMATCH);
	}

	private void assertCode(MockMultipartFile file, String name, String mime, long size, UploadErrorCode code) {
		assertThatThrownBy(() -> validator.validate(name, mime, size, file)).isInstanceOfSatisfying(AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(code));
	}
}
