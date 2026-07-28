package pl.backend.weddinggallery.qr.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.qr.exception.QrErrorCode;
import pl.backend.weddinggallery.qr.model.QrFormat;

class QrCodeServiceTest {
	private final QrCodeService service = new QrCodeService();

	@Test
	void generatesPngAtTheMinimumAllowedSize() {
		var result = service.generate("https://gallery.example/g/reception-abc123", QrFormat.PNG,
				QrCodeService.MIN_SIZE);

		assertThat(result.contentType().toString()).isEqualTo("image/png");
		assertThat(result.content()).startsWith((byte) 0x89, (byte) 0x50, (byte) 0x4e, (byte) 0x47);
	}

	@Test
	void generatesSvgWithoutSecretsAndWithThePublicEntryPoint() {
		String publicUrl = "https://gallery.example/g/reception-abc123";
		var result = service.generate(publicUrl, QrFormat.SVG, 512);
		String svg = new String(result.content(), java.nio.charset.StandardCharsets.UTF_8);

		assertThat(result.contentType().toString()).isEqualTo("image/svg+xml");
		assertThat(svg).startsWith("<?xml").contains("role=\"img\"").contains(publicUrl)
				.doesNotContainIgnoringCase("accessToken", "accessCode", "storageKey", "secret");
	}

	@Test
	void rejectsSizesOutsideTheSupportedNMinusOneAndNPlusOneBoundaries() {
		assertThatThrownBy(
				() -> service.generate("https://gallery.example/g/slug", QrFormat.PNG, QrCodeService.MIN_SIZE - 1))
				.satisfies(error -> assertQrError(error, QrErrorCode.QR_INVALID_SIZE));
		assertThatThrownBy(
				() -> service.generate("https://gallery.example/g/slug", QrFormat.PNG, QrCodeService.MAX_SIZE + 1))
				.satisfies(error -> assertQrError(error, QrErrorCode.QR_INVALID_SIZE));
	}

	@Test
	void rejectsMissingFormatOrUrl() {
		assertThatThrownBy(() -> service.generate("https://gallery.example/g/slug", null, 512))
				.satisfies(error -> assertQrError(error, QrErrorCode.QR_INVALID_FORMAT));
		assertThatThrownBy(() -> service.generate(" ", QrFormat.PNG, 512))
				.satisfies(error -> assertQrError(error, QrErrorCode.QR_GENERATION_FAILED));
	}

	private void assertQrError(Throwable error, QrErrorCode expected) {
		assertThat(error).isInstanceOf(AppException.class);
		assertThat(((AppException) error).getErrorCode()).isEqualTo(expected);
	}
}
