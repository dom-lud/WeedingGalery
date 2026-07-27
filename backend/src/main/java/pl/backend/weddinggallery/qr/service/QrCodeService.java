package pl.backend.weddinggallery.qr.service;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import java.io.ByteArrayOutputStream;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import pl.backend.weddinggallery.common.exception.AppException;
import pl.backend.weddinggallery.qr.exception.QrErrorCode;
import pl.backend.weddinggallery.qr.model.QrFormat;

@Service
@RequiredArgsConstructor
public class QrCodeService {
	public static final int MIN_SIZE = 128;
	public static final int MAX_SIZE = 2048;

	public GeneratedQr generate(String value, QrFormat format, int size) {
		if (format == null)
			throw new AppException(QrErrorCode.QR_INVALID_FORMAT);
		if (size < MIN_SIZE || size > MAX_SIZE)
			throw new AppException(QrErrorCode.QR_INVALID_SIZE);
		if (value == null || value.isBlank())
			throw new AppException(QrErrorCode.QR_GENERATION_FAILED);
		try {
			BitMatrix matrix = new MultiFormatWriter().encode(value, BarcodeFormat.QR_CODE, size, size,
					Map.of(EncodeHintType.MARGIN, 2, EncodeHintType.CHARACTER_SET, StandardCharsets.UTF_8.name()));
			if (format == QrFormat.PNG) {
				ByteArrayOutputStream output = new ByteArrayOutputStream();
				MatrixToImageWriter.writeToStream(matrix, "PNG", output);
				return new GeneratedQr(MediaType.IMAGE_PNG, output.toByteArray());
			}
			return new GeneratedQr(MediaType.parseMediaType(format.mediaType()),
					svg(value, matrix).getBytes(StandardCharsets.UTF_8));
		} catch (Exception exception) {
			throw new AppException(QrErrorCode.QR_GENERATION_FAILED);
		}
	}

	private String svg(String value, BitMatrix matrix) {
		StringBuilder svg = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>")
				.append("<svg xmlns=\"http://www.w3.org/2000/svg\" role=\"img\" aria-label=\"QR code\" viewBox=\"0 0 ")
				.append(matrix.getWidth()).append(' ').append(matrix.getHeight()).append("\"><title>QR code for ")
				.append(escapeXml(value))
				.append("</title><rect width=\"100%\" height=\"100%\" fill=\"white\"/><path fill=\"black\" d=\"");
		for (int y = 0; y < matrix.getHeight(); y++) {
			for (int x = 0; x < matrix.getWidth(); x++) {
				if (matrix.get(x, y))
					svg.append('M').append(x).append(' ').append(y).append("h1v1h-1z");
			}
		}
		return svg.append("\"/></svg>").toString();
	}

	private String escapeXml(String value) {
		return value.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;")
				.replace("'", "&apos;");
	}

	public record GeneratedQr(MediaType contentType, byte[] content) {
	}
}
