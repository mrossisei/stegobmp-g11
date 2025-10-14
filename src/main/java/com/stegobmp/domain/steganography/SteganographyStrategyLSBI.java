package com.stegobmp.domain.steganography;

import java.util.LinkedHashMap;
import java.util.Map;


public class SteganographyStrategyLSBI implements SteganographyStrategy {

    // 4 bytes para el mapa de inversión y 4 bytes (int) para la longitud del payload.
    private static final int MAP_RESERVED_BYTES = 4;
    private static final int LENGTH_METADATA_BYTES = 4;
    private static final int METADATA_TOTAL_BYTES = MAP_RESERVED_BYTES; // private static final int METADATA_TOTAL_BYTES = MAP_RESERVED_BYTES + LENGTH_METADATA_BYTES;

    private static final String[] PATTERNS = {"00", "01", "10", "11"};

    /**
     * Oculta un payload dentro de los datos de píxeles de una imagen usando LSBi.
     * Primero oculta la longitud del payload (4 bytes) y luego el payload.
     */
    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        if (getCapacity(carrierPixelData) < payload.length) {
            throw new IllegalArgumentException("El payload es demasiado grande para el portador.");
        }

        byte[] stegoData = carrierPixelData.clone();

        // Preparar el payload completo: [longitud del payload] + [payload]
        // byte[] payloadLengthBytes = BitUtils.intToBytes(payload.length);
        byte[] fullPayload = new byte[payload.length]; // byte[] fullPayload = new byte[payloadLengthBytes.length + payload.length];
        // System.arraycopy(payloadLengthBytes, 0, fullPayload, 0, payloadLengthBytes.length);
        System.arraycopy(payload, 0, fullPayload, 0, payload.length); // System.arraycopy(payload, 0, fullPayload, payloadLengthBytes.length, payload.length);

        byte[] messageBits = BitUtils.bytesToBits(fullPayload);

        // --- PRIMERA PASADA: ANÁLISIS ---
        Map<String, int[]> changeCounters = analyzeChanges(stegoData, messageBits);

        // --- CREACIÓN DEL MAPA DE INVERSIÓN ---
        Map<String, Boolean> inversionMap = createInversionMap(changeCounters);

        // --- SEGUNDA PASADA: EMBEBIDO ---
        embedData(stegoData, messageBits, inversionMap);

        return stegoData;
    }


    // Extrae un payload oculto de los datos de píxeles de una imagen. TODO: lo deje comentado porque es lo que hizo la IA y no llegue a corregirlo
    @Override
    public byte[] extract(byte[] carrierPixelData, boolean hasExtension) {
//        // Extraer el mapa de inversión de los primeros bytes.
//        Map<String, Boolean> inversionMap = extractInversionMap(carrierPixelData);
//
//        // Extraer la longitud del payload (los siguientes 32 bits/bytes).
//        int payloadLengthInBytes = extractPayloadLength(carrierPixelData, inversionMap); // FIXME: extractPayloadLength no se si está bien hecho
//        int payloadLengthInBits = payloadLengthInBytes * 8; // TODO: ¿Porque?
//
//        // Extraer los bits del payload.
//        byte[] extractedBits = new byte[payloadLengthInBits];
//        int payloadDataStartOffset = METADATA_TOTAL_BYTES;
//
//        for (int i = 0; i < payloadLengthInBits; i++) {
//            int carrierIndex = payloadDataStartOffset + i;
//            byte carrierByte = carrierPixelData[carrierIndex];
//
//            String pattern = BitUtils.getPattern(carrierByte);
//            byte lsb = BitUtils.getLsb(carrierByte);
//
//            if (inversionMap.get(pattern)) {
//                extractedBits[i] = (byte) (lsb == 0 ? 1 : 0); // Invertir bit
//            } else {
//                extractedBits[i] = lsb;
//            }
//        }
//
//        return BitUtils.bitsToBytes(extractedBits);
        return new byte[0];
    }

    // TODO: revisar
    @Override
    public int getCapacity(byte[] carrierPixelData) {
        // (Total de LSBs disponibles - LSBs para metadatos) / 8 bits por byte
        return (carrierPixelData.length - METADATA_TOTAL_BYTES) / 8;
    }


    // --- Métodos privados ---

    // Para cada patrón nos fijamos la cantidad de veces que cambias el LSB y la cantidad de veces que se mantiene
    private Map<String, int[]> analyzeChanges(byte[] carrierData, byte[] messageBits) {
        Map<String, int[]> counters = new LinkedHashMap<>();
        for (String pattern : PATTERNS) {
            counters.put(pattern, new int[2]); // [0] = sin cambio, [1] = con cambio
        }

        for (int i = 0; i < messageBits.length; i++) {
            int carrierIndex = i + MAP_RESERVED_BYTES; // El mapa no se analiza
            updateChangeCounters(counters, carrierData[carrierIndex], messageBits[i]);
        }
        return counters;
    }

    // Actualiza el contador de cambios para un byte portador y un bit de mensaje específicos.
    private void updateChangeCounters(Map<String, int[]> counters, byte carrierByte, byte messageBit) {
        String pattern = BitUtils.getPattern(carrierByte);
        byte carrierLsb = BitUtils.getLsb(carrierByte);

        if (messageBit == carrierLsb) {
            counters.get(pattern)[0]++; // Sin cambios
        } else {
            counters.get(pattern)[1]++; // Con cambios
        }
    }

    // Creación de mapa que indica cuales grupos deben ser invertidos
    private Map<String, Boolean> createInversionMap(Map<String, int[]> changeCounters) {
        Map<String, Boolean> inversionMap = new LinkedHashMap<>();
        for (String pattern : PATTERNS) {
            int[] counts = changeCounters.get(pattern);
            inversionMap.put(pattern, counts[1] > counts[0]);
        }
        return inversionMap;
    }

    // Ocultamos el mensaje en el portador (junto con el "mapa")
    private void embedData(byte[] carrierData, byte[] messageBits, Map<String, Boolean> inversionMap) {
        // Ocultar el mapa de inversión.
        int mapBitIndex = 0;
        for (String pattern : PATTERNS) {
            byte bitToHide = (byte) (inversionMap.get(pattern) ? 1 : 0);
            carrierData[mapBitIndex] = BitUtils.setLsb(carrierData[mapBitIndex], bitToHide);
            mapBitIndex++;
        }

        // Ocultar el mensaje
        for (int i = 0; i < messageBits.length; i++) {
            int carrierIndex = i + MAP_RESERVED_BYTES;
            carrierData[carrierIndex] = hideBitInByte(carrierData[carrierIndex], messageBits[i], inversionMap);
        }
    }

    // Oculta un único bit en un byte, aplicando la lógica de inversión del LSBi
    private byte hideBitInByte(byte carrierByte, byte bitToHide, Map<String, Boolean> inversionMap) {
        String pattern = BitUtils.getPattern(carrierByte);
        boolean mustInvert = inversionMap.get(pattern);

        byte finalBit = bitToHide;
        if (mustInvert) {
            finalBit = (byte) (bitToHide == 0 ? 1 : 0); // Invertir el bit
        }

        return BitUtils.setLsb(carrierByte, finalBit);
    }


    // --- Métodos privados para la extracción ---

    private Map<String, Boolean> extractInversionMap(byte[] carrierPixelData) {
        Map<String, Boolean> inversionMap = new LinkedHashMap<>();
        for (int i = 0; i < MAP_RESERVED_BYTES; i++) {
            byte lsb = BitUtils.getLsb(carrierPixelData[i]);
            inversionMap.put(PATTERNS[i], lsb == 1);
        }
        return inversionMap;
    }


    // FIXME:
    private int extractPayloadLength(byte[] carrierPixelData, Map<String, Boolean> inversionMap) {
        byte[] lengthBits = new byte[LENGTH_METADATA_BYTES * 8];
        int lengthDataStartOffset = MAP_RESERVED_BYTES;

        for (int i = 0; i < lengthBits.length; i++) {
            int carrierIndex = lengthDataStartOffset + i;
            byte carrierByte = carrierPixelData[carrierIndex];

            String pattern = BitUtils.getPattern(carrierByte);
            byte lsb = BitUtils.getLsb(carrierByte);

            if (inversionMap.get(pattern)) {
                lengthBits[i] = (byte) (lsb == 0 ? 1 : 0); // Invertir bit
            } else {
                lengthBits[i] = lsb;
            }
        }

        byte[] lengthBytes = BitUtils.bitsToBytes(lengthBits);
        return BitUtils.bytesToInt(lengthBytes);
    }
}
