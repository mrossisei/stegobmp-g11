//package com.stegobmp.domain.steganography;
//
//import java.io.ByteArrayOutputStream;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//
//public class SteganographyStrategyLSBI implements SteganographyStrategy {
//
//    // 4 bytes para el mapa de inversión y 4 bytes (int) para la longitud del payload.
//    private static final int MAP_RESERVED_BYTES = 4;
//    private static final int METADATA_TOTAL_BYTES = MAP_RESERVED_BYTES; // private static final int METADATA_TOTAL_BYTES = MAP_RESERVED_BYTES + LENGTH_METADATA_BYTES;
//
//    private static final String[] PATTERNS = {"00", "01", "10", "11"};
//
//    private Map<String, Boolean> inversionMap;
//
//    /**
//     * Oculta un payload dentro de los datos de píxeles de una imagen usando LSBi.
//     * Primero oculta la longitud del payload (4 bytes) y luego el payload.
//     */
//    @Override
//    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
//        if (getCapacity(carrierPixelData) < payload.length) {
//            throw new IllegalArgumentException("El payload es demasiado grande para el portador.");
//        }
//
//        byte[] stegoData = carrierPixelData.clone();
//
//        byte[] fullPayload = payload.clone();
//
//        byte[] messageBits = BitUtils.bytesToBits(fullPayload);
//
//        // --- PRIMERA PASADA: ANÁLISIS ---
//        Map<String, int[]> changeCounters = analyzeChanges(stegoData, messageBits);
//
//        // --- CREACIÓN DEL MAPA DE INVERSIÓN (a partir del análisis) ---
//        inversionMap = createInversionMap(changeCounters);
//
//        // --- SEGUNDA PASADA: EMBEBIDO ---
//        embedData(stegoData, messageBits);
//
//        return stegoData;
//    }
//
//
//    @Override
//    public byte[] extract(byte[] carrierPixelData, boolean withExtension) {
//        inversionMap = extractInversionMap(carrierPixelData);
//
//        byte[] payloadSizeInfo = extractPayloadSizeInfo(carrierPixelData, MAP_RESERVED_BYTES);
//
//        // Convertir Big Endian 4 bytes a int
//        int payloadLength = convertPayloadLength(payloadSizeInfo) + 4;
//
//        byte[] extractedPayload = extractPayload(carrierPixelData, payloadLength);
//
//        if (!withExtension) {
//            return extractedPayload;
//        }
//
//        int carrierIndex = payloadLength * 8; // Mover el índice después del payload extraído
//
//        ByteArrayOutputStream extensionStream = new ByteArrayOutputStream();
//        while (carrierPixelData.length > carrierIndex) {
//            byte b = extractByte(carrierPixelData, carrierIndex);
//            carrierIndex += 8;
//            extensionStream.write(b);
//            if (b == '\0') {
//                break;
//            }
//        }
//        byte[] extensionPayload = extensionStream.toByteArray();
//
//        byte[] finalPayload = new byte[payloadLength + extensionPayload.length];
//        System.arraycopy(extractedPayload, 0, finalPayload, 0, payloadLength);
//        System.arraycopy(extensionPayload, 0, finalPayload, payloadLength, extensionPayload.length);
//        extractedPayload = finalPayload;
//
//
//        return extractedPayload;
//    }
//
//    // TODO: revisar
//    @Override
//    public int getCapacity(byte[] carrierPixelData) {
//        // (Total de LSBs disponibles - LSBs para metadatos) / 8 bits por byte
//        return (carrierPixelData.length - METADATA_TOTAL_BYTES) / 8;
//    }
//
//
//    // --- Métodos privados ---
//
//
//
//    // consume 8 bytes de carrierPixelData para extraer el byte escondido
//    private byte extractByte(byte[] carrierPixelData, int startBitIndex) {
//        byte b = 0;
//        for (int bit = 7; bit >= 0; bit--) {
//            byte carrierByte = carrierPixelData[startBitIndex];
//            String pattern = BitUtils.getPattern(carrierByte);
//            byte lsb = BitUtils.getLsb(carrierByte);
//
//            if (inversionMap.get(pattern)) {
//                lsb = (byte) (lsb == 0 ? 1 : 0); // invertirlo
//            }
//
//            b |= (byte) (lsb << bit);
//            startBitIndex++;
//        }
//        return b;
//    }
//
//    private byte[] extractPayloadSizeInfo(byte[] carrierPixel, int carrierIndex) {
//        byte[] payloadSizeInfo = new byte[4];
//
//        // ---- Extraer los 4 bytes que contienen el tamaño del payload ----
//        for (int i = 0; i < 4; i++) {
//            byte b = extractByte(carrierPixel, carrierIndex);
//            payloadSizeInfo[i] = b;
//            carrierIndex += 8;
//        }
//        return payloadSizeInfo;
//    }
//
//    private int convertPayloadLength(byte[] payloadSizeInfo) {
//        return ((payloadSizeInfo[0] & 0xFF) << 24) |
//                ((payloadSizeInfo[1] & 0xFF) << 16) |
//                ((payloadSizeInfo[2] & 0xFF) << 8)  |
//                (payloadSizeInfo[3] & 0xFF);
//    }
//
//
//    private byte[] extractPayload(byte[] carrierPixelData, int payloadLength) {
//        byte[] extractedPayload = new byte[payloadLength];
//        int startBitIndex = 0;
//
//        for (int i = 0; i < payloadLength; i++) {
//            byte b = extractByte(carrierPixelData, startBitIndex);
//            extractedPayload[i] = b;
//            startBitIndex += 8;
//        }
//        return extractedPayload;
//    }
//
//
//    // Para cada patrón nos fijamos la cantidad de veces que cambias el LSB y la cantidad de veces que se mantiene
//    private Map<String, int[]> analyzeChanges(byte[] carrierData, byte[] messageBits) {
//        Map<String, int[]> counters = new LinkedHashMap<>();
//        for (String pattern : PATTERNS) {
//            counters.put(pattern, new int[2]); // [0] = sin cambio, [1] = con cambio
//        }
//
//        for (int i = 0; i < messageBits.length; i++) {
//            int carrierIndex = i + MAP_RESERVED_BYTES; // El mapa no se analiza
//            updateChangeCounters(counters, carrierData[carrierIndex], messageBits[i]);
//        }
//        return counters;
//    }
//
//    // Actualiza el contador de cambios para un byte portador y un bit de mensaje específicos.
//    private void updateChangeCounters(Map<String, int[]> counters, byte carrierByte, byte messageBit) {
//        String pattern = BitUtils.getPattern(carrierByte);
//        byte carrierLsb = BitUtils.getLsb(carrierByte);
//
//        if (messageBit == carrierLsb) {
//            counters.get(pattern)[0]++; // Sin cambios
//        } else {
//            counters.get(pattern)[1]++; // Con cambios
//        }
//    }
//
//    // Creación de mapa que indica cuales grupos deben ser invertidos
//    private Map<String, Boolean> createInversionMap(Map<String, int[]> changeCounters) {
//        Map<String, Boolean> inversionMap = new LinkedHashMap<>();
//        for (String pattern : PATTERNS) {
//            int[] counts = changeCounters.get(pattern);
//            inversionMap.put(pattern, counts[1] > counts[0]);
//        }
//        return inversionMap;
//    }
//
//    // Ocultamos el mensaje en el portador (junto con el "mapa")
//    private void embedData(byte[] carrierData, byte[] messageBits) {
//        // Ocultar el mapa de inversión.
//        int mapBitIndex = 0;
//        for (String pattern : PATTERNS) {
//            byte bitToHide = (byte) (inversionMap.get(pattern) ? 1 : 0);
//            carrierData[mapBitIndex] = BitUtils.setLsb(carrierData[mapBitIndex], bitToHide);
//            mapBitIndex++;
//        }
//
//        // Ocultar el mensaje
//        for (int i = 0; i < messageBits.length; i++) {
//            int carrierIndex = i + MAP_RESERVED_BYTES;
//            carrierData[carrierIndex] = hideBitInByte(carrierData[carrierIndex], messageBits[i], inversionMap);
//        }
//    }
//
//    // Oculta un único bit en un byte, aplicando la lógica de inversión del LSBi
//    private byte hideBitInByte(byte carrierByte, byte bitToHide, Map<String, Boolean> inversionMap) {
//        String pattern = BitUtils.getPattern(carrierByte);
//        boolean mustInvert = inversionMap.get(pattern);
//
//        byte finalBit = bitToHide;
//        if (mustInvert) {
//            finalBit = (byte) (bitToHide == 0 ? 1 : 0); // Invertir el bit
//        }
//
//        return BitUtils.setLsb(carrierByte, finalBit);
//    }
//
//
//    // --- Métodos privados para la extracción ---
//
//    private Map<String, Boolean> extractInversionMap(byte[] carrierPixelData) {
//        Map<String, Boolean> inversionMap = new LinkedHashMap<>();
//        for (int i = 0; i < MAP_RESERVED_BYTES; i++) {
//            byte lsb = BitUtils.getLsb(carrierPixelData[i]);
//            inversionMap.put(PATTERNS[i], lsb == 1);
//        }
//        return inversionMap;
//    }
//
//
////    private int extractPayloadLength(byte[] carrierPixelData, Map<String, Boolean> inversionMap) {
////        byte[] lengthBits = new byte[LENGTH_METADATA_BYTES * 8];
////
////        for (int i = 0; i < lengthBits.length; i++) {
////            int carrierIndex = MAP_RESERVED_BYTES + i;
////            byte carrierByte = carrierPixelData[carrierIndex];
////
////            String pattern = BitUtils.getPattern(carrierByte);
////            byte lsb = BitUtils.getLsb(carrierByte);
////
////            if (inversionMap.get(pattern)) {
////                lengthBits[i] = (byte) (lsb == 0 ? 1 : 0); // Invertir bit
////            } else {
////                lengthBits[i] = lsb;
////            }
////        }
////
////        byte[] lengthBytes = BitUtils.bitsToBytes(lengthBits);
////        return BitUtils.bytesToInt(lengthBytes);
////    }
//}
