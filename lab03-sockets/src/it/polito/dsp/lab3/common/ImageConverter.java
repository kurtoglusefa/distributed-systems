package it.polito.dsp.lab3.common;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import javax.imageio.ImageIO;

public class ImageConverter {

    public static byte[] convert(byte[] inputBytes,
                                 String originType,
                                 String targetType) throws IOException {

        ByteArrayInputStream bais = new ByteArrayInputStream(inputBytes);
        BufferedImage img = ImageIO.read(bais);
        if (img == null) {
            throw new IOException("Unsupported image content");
        }

        if (img.getColorModel().getTransparency() != Transparency.OPAQUE) {
            img = fillTransparentPixels(img, Color.WHITE);
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(img, targetType.toLowerCase(), baos);
        return baos.toByteArray();
    }

    private static BufferedImage fillTransparentPixels(BufferedImage image, Color fillColor) {
        int w = image.getWidth();
        int h = image.getHeight();
        BufferedImage image2 = new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB);
        Graphics2D g = image2.createGraphics();
        g.setColor(fillColor);
        g.fillRect(0, 0, w, h);
        g.drawRenderedImage(image, null);
        g.dispose();
        return image2;
    }
}
