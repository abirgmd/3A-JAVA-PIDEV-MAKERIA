package com.abircode.cruddp.services.user;

import com.abircode.cruddp.entities.User;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.paint.Color;

import java.util.HashMap;
import java.util.Map;

public class QRCodeService {
    public Image generateQRCodeImage(String text, int width, int height) throws WriterException {
        Map<EncodeHintType, Object> hints = new HashMap<>();
        hints.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.H);
        hints.put(EncodeHintType.MARGIN, 2);
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");

        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, width, height, hints);

        WritableImage image = new WritableImage(width, height);
        PixelWriter pixelWriter = image.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                pixelWriter.setColor(x, y, bitMatrix.get(x, y) ? Color.BLACK : Color.WHITE);
            }
        }

        return image;
    }

    public String generateUserQRCodeData(User user) {
        return String.format("Name: %s | Lastname: %s | Email: %s",
                user.getName(), user.getLastname(), user.getEmail());
    }
}