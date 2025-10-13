package com.stegobmp.domain.steganography;


import com.stegobmp.exception.StegoException;

public enum StegAlgorithm {
    LSB1,    // LSB de 1 bit
    LSB4,    // LSB de 4 bits
    LSBI;     // LSB Improved

    public static StegAlgorithm fromString(String text) throws StegoException {
        try {
            return StegAlgorithm.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StegoException("Algoritmo de esteganografía no válido: '" + text + "'. Use LSB1, LSB4 o LSBI.");
        }
    }
}