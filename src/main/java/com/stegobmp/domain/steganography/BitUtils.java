package com.stegobmp.domain.steganography;

import java.nio.ByteBuffer;


public final class BitUtils {


    private BitUtils() {
        throw new UnsupportedOperationException("Esta es una clase de utilidad y no puede ser instanciada.");
    }

    // Extrae el patrón formado por el 2do y 3er bit menos significativo de un byte: Retorna alguna de estas: "00", "01", "10", "11"
    public static String getPattern(byte b) {
        return "" + ((b >> 2) & 1) + ((b >> 1) & 1);
    }

    //Obtiene el bit menos significativo (LSB) de un byte.
    public static byte getLsb(byte b) {
        return (byte) (b & 1);
    }

    //Establece el bit menos significativo (LSB) de un byte a un valor específico.
    public static byte setLsb(byte b, byte bit) {
        return (byte) ((b & 0xFE) | bit);
    }

    /**
     * Convierte un arreglo de bytes en un arreglo de bits (representados como bytes 0 o 1).
     *
     * @param data Los bytes del mensaje.
     * @return Un arreglo de bytes donde cada elemento es 0 o 1.
     */
    public static byte[] bytesToBits(byte[] data) {
        byte[] bits = new byte[data.length * 8];
        for (int i = 0; i < data.length; i++) {
            byte currentByte = data[i];
            for (int j = 0; j < 8; j++) {
                bits[i * 8 + j] = (byte) ((currentByte >> (7 - j)) & 1);
            }
        }
        return bits;
    }

    /**
     * Convierte un arreglo de bits (bytes 0 o 1) en un arreglo de bytes.
     *
     * @param bits El arreglo de bits. Debe tener una longitud múltiplo de 8.
     * @return El arreglo de bytes reconstruido.
     */
    public static byte[] bitsToBytes(byte[] bits) {
        if (bits.length % 8 != 0) {
            throw new IllegalArgumentException("La longitud del arreglo de bits debe ser un múltiplo de 8.");
        }
        byte[] data = new byte[bits.length / 8];
        for (int i = 0; i < data.length; i++) {
            byte currentByte = 0;
            for (int j = 0; j < 8; j++) {
                if (bits[i * 8 + j] == 1) {
                    currentByte |= (1 << (7 - j));
                }
            }
            data[i] = currentByte;
        }
        return data;
    }

    /**
     * Convierte un entero a un arreglo de 4 bytes (big-endian).
     */
    public static byte[] intToBytes(final int i) {
        return ByteBuffer.allocate(4).putInt(i).array();
    }

    /**
     * Convierte un arreglo de 4 bytes a un entero (big-endian).
     */
    public static int bytesToInt(byte[] bytes) {
        if (bytes.length != 4) {
            throw new IllegalArgumentException("El arreglo debe contener exactamente 4 bytes para convertir a entero.");
        }
        return ByteBuffer.wrap(bytes).getInt();
    }
}
