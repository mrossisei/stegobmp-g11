package com.stegobmp.domain.bmp;

import com.stegobmp.exception.StegoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

public class BmpHandler {

    private static final short BITS_PER_PIXEL_REQUIRED = 24;
    private static final int COMPRESSION_METHOD_REQUIRED = 0; // BI_RGB

    public BmpImage parseBmp(byte[] bmpData) {
        int headerSize = getHeaderSize(bmpData);
        if (bmpData.length < headerSize) {
            throw new StegoException("Datos BMP inválidos: el tamaño es menor que el del encabezado.");
        }

        byte[] header = Arrays.copyOfRange(bmpData, 0, headerSize);
        validateHeader(header);

        byte[] pixelData = Arrays.copyOfRange(bmpData, headerSize, bmpData.length);
        return new BmpImage(header, pixelData);
    }

    public byte[] writeBmpToBytes(BmpImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(image.header());
            outputStream.write(image.pixelData());
            return outputStream.toByteArray();
        } catch (IOException e) {
            throw new StegoException("Error al escribir el BMP en el buffer de memoria.", e);
        }
    }

    private void validateHeader(byte[] header) {
        ByteBuffer headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

        if (headerBuffer.get() != 'B' || headerBuffer.get() != 'M') {
            throw new StegoException("El archivo no es un BMP válido (firma incorrecta).");
        }
        if (headerBuffer.getShort(28) != BITS_PER_PIXEL_REQUIRED) {
            throw new StegoException("El archivo BMP debe ser de 24 bits por píxel.");
        }
        if (headerBuffer.getInt(30) != COMPRESSION_METHOD_REQUIRED) {
            throw new StegoException("El archivo BMP no debe tener compresión.");
        }
    }
    private int getHeaderSize(byte[] bmpData) {
        if (bmpData.length < 14) {
            throw new StegoException("Datos BMP inválidos: el tamaño es menor que el encabezado mínimo.");
        }

        ByteBuffer buffer = ByteBuffer.wrap(bmpData).order(ByteOrder.LITTLE_ENDIAN);

        return buffer.getInt(10);
    }
}

