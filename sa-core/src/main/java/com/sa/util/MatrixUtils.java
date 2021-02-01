package com.sa.util;

import com.google.zxing.*;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Hashtable;


public final class MatrixUtils {

    private static final String CHARSET = "utf-8";
    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;


    private MatrixUtils() {
    }


    public static BitMatrix toQRCodeMatrix(String text, Integer width,
                                           Integer height) {
        if (width == null || width < 300) {
            width = 300;
        }

        if (height == null || height < 300) {
            height = 300;
        }

        Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();

        hints.put(EncodeHintType.CHARACTER_SET, CHARSET);
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = new MultiFormatWriter().encode(text,
                    BarcodeFormat.QR_CODE, width, height, hints);
        } catch (WriterException e) {

            e.printStackTrace();
        }



        return bitMatrix;
    }


    public boolean toQrcodeFile(String text, File file, String format) {
        BitMatrix matrix = toQRCodeMatrix(text, null, null);
        if (matrix != null) {
            try {
                writeToFile(matrix, format, file);
                return true;
            } catch (IOException e) {

                e.printStackTrace();
            }
        }
        return false;
    }


    public static BufferedImage toBufferedImage(BitMatrix matrix) {
        int width = matrix.getWidth();
        int height = matrix.getHeight();
        BufferedImage image = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                image.setRGB(x, y, matrix.get(x, y) ? BLACK : WHITE);
            }
        }
        return image;
    }


    public static BitMatrix toBarCodeMatrix(String str, Integer width,
                                            Integer height) {
        try {

            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, CHARSET);

            BitMatrix bitMatrix = new MultiFormatWriter().encode(str,
                    BarcodeFormat.CODE_128, width, height, hints);

            return bitMatrix;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void writeToFile(BitMatrix matrix, String format, File file)
            throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, file)) {
            throw new IOException("Could not write an image of format "
                    + format + " to " + file);
        }
    }


    public static void writeToStream(BitMatrix matrix, String format,
                                     OutputStream stream) throws IOException {
        BufferedImage image = toBufferedImage(matrix);
        if (!ImageIO.write(image, format, stream)) {
            throw new IOException("Could not write an image of format "
                    + format);
        }
    }


    public static String decode(File file) {

        BufferedImage image;
        try {
            if (file == null || file.exists() == false) {
                throw new Exception(" File not found:" + file.getPath());
            }

            image = ImageIO.read(file);

            LuminanceSource source = new BufferedImageLuminanceSource(image);
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));

            Result result;


            Hashtable hints = new Hashtable();
            hints.put(DecodeHintType.CHARACTER_SET, CHARSET);

            result = new MultiFormatReader().decode(bitmap, hints);

            return result.getText();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}