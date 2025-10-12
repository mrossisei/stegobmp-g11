package com.tudai.stegobmp.domain.bmp;

import com.tudai.stegobmp.exception.StegoException;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Arrays;

/**
 * Responsable de parsear y construir arrays de bytes con formato BMP.
 */
public class BmpHandler {

    public static final int HEADER_SIZE = 54;
    private static final short BITS_PER_PIXEL_REQUIRED = 24;
    private static final int COMPRESSION_METHOD_REQUIRED = 0; // BI_RGB

    /**
     * Parsea un array de bytes, lo valida y lo convierte en un objeto BmpImage.
     *
     * @param bmpData El array de bytes completo del archivo BMP.
     * @return Un objeto BmpImage que contiene el encabezado y los datos de los píxeles.
     */
    public BmpImage parseBmp(byte[] bmpData) {
        if (bmpData.length < HEADER_SIZE) {
            throw new StegoException("Datos BMP inválidos: el tamaño es menor que el del encabezado.");
        }

        byte[] header = Arrays.copyOfRange(bmpData, 0, HEADER_SIZE);
        validateHeader(header);

        byte[] pixelData = Arrays.copyOfRange(bmpData, HEADER_SIZE, bmpData.length);
        return new BmpImage(header, pixelData);
    }

    /**
     * Construye un array de bytes a partir de un objeto BmpImage.
     *
     * @param image El objeto BmpImage a convertir.
     * @return Un array de bytes que representa el archivo BMP completo.
     */
    public byte[] writeBmpToBytes(BmpImage image) {
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            outputStream.write(image.header());
            outputStream.write(image.pixelData());
            return outputStream.toByteArray();
        } catch (IOException e) {
            // Esta excepción es muy improbable con ByteArrayOutputStream, pero es buena práctica manejarla.
            throw new StegoException("Error al escribir el BMP en el buffer de memoria.", e);
        }
    }

    private void validateHeader(byte[] header) {
        ByteBuffer headerBuffer = ByteBuffer.wrap(header).order(ByteOrder.LITTLE_ENDIAN);

        // Validar firma 'BM'
        if (headerBuffer.get() != 'B' || headerBuffer.get() != 'M') {
            throw new StegoException("El archivo no es un BMP válido (firma incorrecta).");
        }
        // Validar 24 bits por píxel
        if (headerBuffer.getShort(28) != BITS_PER_PIXEL_REQUIRED) {
            throw new StegoException("El archivo BMP debe ser de 24 bits por píxel.");
        }
        // Validar sin compresión
        if (headerBuffer.getInt(30) != COMPRESSION_METHOD_REQUIRED) {
            throw new StegoException("El archivo BMP no debe tener compresión.");
        }
    }
}

