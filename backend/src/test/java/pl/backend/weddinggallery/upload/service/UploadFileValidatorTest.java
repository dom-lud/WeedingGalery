package pl.backend.weddinggallery.upload.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.concurrent.Semaphore;
import javax.imageio.ImageIO;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.media.model.MediaType;
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
	void acceptsSupportedFormatsAndNormalizesExtensionAndMimeCase() {
		byte[] png = image("png");
		assertDetected("photo.PNG", "IMAGE/PNG", png, MediaType.IMAGE, "image/png");
		assertDetected("lossless.webp", "image/webp", webp("VP8L"), MediaType.IMAGE, "image/webp");
		assertDetected("lossy.webp", "image/webp", webp("VP8 "), MediaType.IMAGE, "image/webp");
		assertDetected("extended.webp", "image/webp", webp("VP8X"), MediaType.IMAGE, "image/webp");
		assertDetected("clip.mp4", "video/mp4", mp4(), MediaType.VIDEO, "video/mp4");
	}

	@Test
	void declarationChecksTypeAndNMinusOneNAndNPlusOneLimits() {
		assertThat(validator.validateDeclaration("photo.jpeg", "image/jpeg", 2047).maxBytes()).isEqualTo(2048);
		assertThat(validator.validateDeclaration("photo.jpg", "image/jpeg", 2048).mediaType())
				.isEqualTo(MediaType.IMAGE);
		assertThatThrownBy(() -> validator.validateDeclaration("photo.jpg", "image/jpeg", 2049)).isInstanceOfSatisfying(
				AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(UploadErrorCode.UPLOAD_FILE_TOO_LARGE));
		assertThatThrownBy(() -> validator.validateDeclaration("photo", "image/jpeg", 1)).isInstanceOfSatisfying(
				AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(UploadErrorCode.UPLOAD_TYPE_NOT_ALLOWED));
		assertThatThrownBy(() -> validator.validateDeclaration("photo.png", "image/jpeg", 1)).isInstanceOfSatisfying(
				AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(UploadErrorCode.UPLOAD_TYPE_NOT_ALLOWED));
	}

	@Test
	void stripsClientPathButRejectsUnsafeOrInvalidOriginalNames() {
		byte[] jpeg = jpeg();
		var windowsPath = new MockMultipartFile("file", "C:\\fakepath\\photo.jpg", "image/jpeg", jpeg);
		assertThat(validator.validate("photo.jpg", "image/jpeg", jpeg.length, windowsPath).mediaType())
				.isEqualTo(MediaType.IMAGE);

		assertInvalidName(null);
		assertInvalidName("bad\0name.jpg");
		assertInvalidName("/");
		assertInvalidName("a".repeat(252) + ".jpg");
		assertCode(new MockMultipartFile("file", "other.jpg", "image/jpeg", jpeg), "photo.jpg", "image/jpeg",
				jpeg.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
	}

	@Test
	void rejectsTruncatedMalformedAndStructurallyInvalidMedia() {
		byte[] jpeg = jpeg();
		assertCode(new MockMultipartFile("file", "photo.jpg", "image/jpeg", java.util.Arrays.copyOf(jpeg, 8)),
				"photo.jpg", "image/jpeg", 8, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		byte[] png = image("png");
		png[png.length - 1] = 0;
		assertCode(new MockMultipartFile("file", "photo.png", "image/png", png), "photo.png", "image/png", png.length,
				UploadErrorCode.UPLOAD_CONTENT_MISMATCH);

		for (byte[] invalid : new byte[][]{new byte[20], webpWithInvalidRiffSize(), webp("NONE")}) {
			assertCode(new MockMultipartFile("file", "photo.webp", "image/webp", invalid), "photo.webp", "image/webp",
					invalid.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		}
		for (byte[] invalid : new byte[][]{new byte[11], mp4WithoutRequiredBox("mdat"), mp4WithoutRequiredBox("moov"),
				mp4WithUnsupportedBrand(), mp4WithInvalidBoxSize()}) {
			assertCode(new MockMultipartFile("file", "clip.mp4", "video/mp4", invalid), "clip.mp4", "video/mp4",
					invalid.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		}
	}

	@Test
	void rejectsUnreadableAndOversizedImageContent() throws Exception {
		MultipartFile broken = mock(MultipartFile.class);
		when(broken.isEmpty()).thenReturn(false);
		when(broken.getSize()).thenReturn(4L);
		when(broken.getOriginalFilename()).thenReturn("photo.jpg");
		when(broken.getInputStream()).thenThrow(new IOException("broken stream"));
		assertThatThrownBy(() -> validator.validate("photo.jpg", "image/jpeg", 4, broken)).isInstanceOfSatisfying(
				AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(UploadErrorCode.UPLOAD_CONTENT_MISMATCH));

		ReflectionTestUtils.setField(validator, "maxImagePixels", 0L);
		byte[] jpeg = jpeg();
		assertCode(new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpeg), "photo.jpg", "image/jpeg",
				jpeg.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
	}

	@Test
	void rejectsWhenImageValidationCapacityIsExhausted() throws Exception {
		Semaphore slots = (Semaphore) ReflectionTestUtils.getField(validator, "imageDecodeSlots");
		assertThat(slots).isNotNull();
		slots.acquire(2);
		try {
			byte[] jpeg = jpeg();
			assertCode(new MockMultipartFile("file", "photo.jpg", "image/jpeg", jpeg), "photo.jpg", "image/jpeg",
					jpeg.length, UploadErrorCode.UPLOAD_VALIDATION_BUSY);
		} finally {
			slots.release(2);
		}
	}

	@Test
	void rejectsSignatureOnlyPayloads() {
		assertInvalidContent("photo.jpg", "image/jpeg", new byte[]{(byte) 0xff, (byte) 0xd8, (byte) 0xff});
		byte[] fakeJpeg = {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xe0};
		assertCode(new MockMultipartFile("file", "photo.jpg", "image/jpeg", fakeJpeg), "photo.jpg", "image/jpeg",
				fakeJpeg.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
		byte[] fakeMp4 = {0, 0, 0, 12, 'f', 't', 'y', 'p', 'i', 's', 'o', 'm'};
		assertCode(new MockMultipartFile("file", "video.mp4", "video/mp4", fakeMp4), "video.mp4", "video/mp4",
				fakeMp4.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
	}

	@Test
	void rejectsEveryFileLevelBoundaryBeforeParsingContent() throws Exception {
		byte[] jpeg = jpeg();
		MultipartFile tooLarge = mock(MultipartFile.class);
		when(tooLarge.isEmpty()).thenReturn(false);
		when(tooLarge.getSize()).thenReturn(2049L);
		when(tooLarge.getOriginalFilename()).thenReturn("photo.jpg");
		assertThatThrownBy(() -> validator.validate("photo.jpg", "image/jpeg", 2049, tooLarge)).isInstanceOfSatisfying(
				AppException.class,
				ex -> assertThat(ex.getErrorCode()).isEqualTo(UploadErrorCode.UPLOAD_FILE_TOO_LARGE));

		String nameAtLimit = "a".repeat(251) + ".jpg";
		assertThat(validator.validate(nameAtLimit, "image/jpeg", jpeg.length,
				new MockMultipartFile("file", "/tmp/" + nameAtLimit, "image/jpeg", jpeg)).mediaType())
				.isEqualTo(MediaType.IMAGE);
	}

	@Test
	void rejectsEachJpegAndPngSignatureBoundaryAndUnreadablePayload() {
		byte[] jpeg = jpeg();
		for (int index = 0; index < 3; index++) {
			byte[] invalid = jpeg.clone();
			invalid[index] = 0;
			assertInvalidContent("photo.jpg", "image/jpeg", invalid);
		}
		byte[] missingJpegEoi = jpeg.clone();
		missingJpegEoi[missingJpegEoi.length - 2] = 0;
		assertInvalidContent("photo.jpg", "image/jpeg", missingJpegEoi);

		byte[] png = image("png");
		for (int index = 0; index < 8; index++) {
			byte[] invalid = png.clone();
			invalid[index] ^= 1;
			assertInvalidContent("photo.png", "image/png", invalid);
		}
		byte[] missingPngTrailer = png.clone();
		missingPngTrailer[missingPngTrailer.length - 12] = 1;
		assertInvalidContent("photo.png", "image/png", missingPngTrailer);
		assertInvalidContent("photo.png", "image/png", new byte[]{(byte) 0x89, 'P', 'N', 'G', 13, 10, 26, 10});
	}

	@Test
	void rejectsEveryWebpStructuralBoundary() {
		for (int length : new int[]{0, 20})
			assertInvalidContent("photo.webp", "image/webp", new byte[length]);
		for (int index : new int[]{0, 8}) {
			byte[] invalid = webp("VP8L");
			invalid[index] ^= 1;
			assertInvalidContent("photo.webp", "image/webp", invalid);
		}
		byte[] invalidLosslessMarker = webp("VP8L");
		invalidLosslessMarker[20] = 0;
		assertInvalidContent("photo.webp", "image/webp", invalidLosslessMarker);
		byte[] shortLossy = java.util.Arrays.copyOf(webp("VP8 "), 25);
		littleEndian(shortLossy, 4, shortLossy.length - 8);
		assertInvalidContent("photo.webp", "image/webp", shortLossy);
		for (int index : new int[]{23, 24, 25}) {
			byte[] invalid = webp("VP8 ");
			invalid[index] ^= 1;
			assertInvalidContent("photo.webp", "image/webp", invalid);
		}
		byte[] shortExtended = java.util.Arrays.copyOf(webp("VP8X"), 29);
		littleEndian(shortExtended, 4, shortExtended.length - 8);
		assertInvalidContent("photo.webp", "image/webp", shortExtended);
		byte[] invalidExtendedSize = webp("VP8X");
		littleEndian(invalidExtendedSize, 16, 9);
		assertInvalidContent("photo.webp", "image/webp", invalidExtendedSize);
	}

	@Test
	void acceptsAllSupportedMp4BrandsAndRejectsBoxCornerCases() {
		for (String brand : new String[]{"isom", "iso2", "mp41", "mp42", "avc1"})
			assertDetected("clip.mp4", "video/mp4", concat(ftyp(brand), box("free", new byte[]{1}),
					box("mdat", new byte[]{1}), box("moov", new byte[]{1})), MediaType.VIDEO, "video/mp4");

		byte[] wrongType = mp4();
		wrongType[4] = 'x';
		assertInvalidContent("clip.mp4", "video/mp4", wrongType);
		byte[] tooSmallFtyp = mp4();
		bigEndian(tooSmallFtyp, 0, 15);
		assertInvalidContent("clip.mp4", "video/mp4", tooSmallFtyp);
		assertInvalidContent("clip.mp4", "video/mp4",
				concat(ftyp("isom"), box("mdat", new byte[0]), box("moov", new byte[]{1})));
		assertInvalidContent("clip.mp4", "video/mp4",
				concat(ftyp("isom"), box("mdat", new byte[]{1}), box("moov", new byte[0])));
		assertInvalidContent("clip.mp4", "video/mp4", concat(mp4(), new byte[]{1}));
		byte[] boxBelowHeader = mp4();
		bigEndian(boxBelowHeader, 16, 7);
		assertInvalidContent("clip.mp4", "video/mp4", boxBelowHeader);
	}

	private void assertInvalidContent(String name, String mime, byte[] content) {
		assertCode(new MockMultipartFile("file", name, mime, content), name, mime, content.length,
				UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
	}

	private byte[] jpeg() {
		return image("jpg");
	}

	private byte[] image(String format) {
		try {
			ByteArrayOutputStream output = new ByteArrayOutputStream();
			ImageIO.write(new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB), format, output);
			return output.toByteArray();
		} catch (Exception ex) {
			throw new AssertionError(ex);
		}
	}

	private void assertDetected(String name, String mime, byte[] content, MediaType mediaType, String detectedType) {
		var result = validator.validate(name, mime, content.length, new MockMultipartFile("file", name, mime, content));
		assertThat(result.mediaType()).isEqualTo(mediaType);
		assertThat(result.detectedContentType()).isEqualTo(detectedType);
	}

	private void assertInvalidName(String originalName) {
		byte[] jpeg = jpeg();
		assertCode(new MockMultipartFile("file", originalName, "image/jpeg", jpeg), "photo.jpg", "image/jpeg",
				jpeg.length, UploadErrorCode.UPLOAD_CONTENT_MISMATCH);
	}

	private byte[] webp(String chunk) {
		int length = switch (chunk) {
			case "VP8 " -> 26;
			case "VP8X" -> 30;
			default -> 21;
		};
		byte[] value = new byte[length];
		ascii(value, 0, "RIFF");
		littleEndian(value, 4, length - 8);
		ascii(value, 8, "WEBP");
		ascii(value, 12, chunk);
		if ("VP8L".equals(chunk))
			value[20] = 0x2f;
		if ("VP8 ".equals(chunk)) {
			value[23] = (byte) 0x9d;
			value[24] = 0x01;
			value[25] = 0x2a;
		}
		if ("VP8X".equals(chunk))
			littleEndian(value, 16, 10);
		return value;
	}

	private byte[] webpWithInvalidRiffSize() {
		byte[] value = webp("VP8L");
		littleEndian(value, 4, 1);
		return value;
	}

	private byte[] mp4() {
		return concat(ftyp("isom"), box("mdat", new byte[]{1}), box("moov", new byte[]{1}));
	}

	private byte[] mp4WithoutRequiredBox(String excluded) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		output.writeBytes(ftyp("isom"));
		if (!"mdat".equals(excluded))
			output.writeBytes(box("mdat", new byte[]{1}));
		if (!"moov".equals(excluded))
			output.writeBytes(box("moov", new byte[]{1}));
		return output.toByteArray();
	}

	private byte[] mp4WithUnsupportedBrand() {
		return concat(ftyp("bad!"), box("mdat", new byte[]{1}), box("moov", new byte[]{1}));
	}

	private byte[] ftyp(String brand) {
		return box("ftyp", concat(ascii(brand), new byte[4]));
	}

	private byte[] mp4WithInvalidBoxSize() {
		byte[] value = mp4();
		value[16] = 0x7f;
		return value;
	}

	private byte[] box(String type, byte[] payload) {
		byte[] value = new byte[8 + payload.length];
		bigEndian(value, 0, value.length);
		ascii(value, 4, type);
		System.arraycopy(payload, 0, value, 8, payload.length);
		return value;
	}

	private byte[] concat(byte[]... values) {
		ByteArrayOutputStream output = new ByteArrayOutputStream();
		for (byte[] value : values)
			output.writeBytes(value);
		return output.toByteArray();
	}

	private byte[] ascii(String text) {
		return text.getBytes(java.nio.charset.StandardCharsets.US_ASCII);
	}

	private void ascii(byte[] target, int offset, String text) {
		byte[] value = ascii(text);
		System.arraycopy(value, 0, target, offset, value.length);
	}

	private void littleEndian(byte[] target, int offset, int value) {
		target[offset] = (byte) value;
		target[offset + 1] = (byte) (value >>> 8);
		target[offset + 2] = (byte) (value >>> 16);
		target[offset + 3] = (byte) (value >>> 24);
	}

	private void bigEndian(byte[] target, int offset, int value) {
		target[offset] = (byte) (value >>> 24);
		target[offset + 1] = (byte) (value >>> 16);
		target[offset + 2] = (byte) (value >>> 8);
		target[offset + 3] = (byte) value;
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
