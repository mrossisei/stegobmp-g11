package com.stegobmp.domain.steganography;

import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;


public class SteganographyStrategyLSBI extends SteganographyStrategyAbs {
    private static final int[] PATTERNS = {0, 2, 4, 6}; // Patrones 00, 01, 10, 11 (bits 2 y 1)
    private static final int METADATA_TOTAL_BYTES = 4;
    private final Map<Integer, Boolean> inversionMap = new HashMap<>();


    private record ExtractResult(byte extractedByte, int nextIndex) {
    }

    public SteganographyStrategyLSBI() {
        super(StegAlgorithm.LSBI);
    }



    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        if (payload.length > getCapacity(carrierPixelData)) {
            throw new IllegalArgumentException("Payload es demasiado grande (" + payload.length + " bytes) para la capacidad del portador (" + getCapacity(carrierPixelData) + " bytes) usando LSBI.");
        }
        return super.embed(carrierPixelData, payload);
    }

    @Override
    protected byte[] modifyCarrierData(byte[] carrierPixelData, byte[] payload) {
        Map<Integer, int[]> counters = new LinkedHashMap<>();
        initializeCounters(counters);

        analyzePayload(carrierPixelData, payload, counters);

        fillInversionMap(counters);

        byte[] modified = carrierPixelData.clone();


        obfuscateInversionMap(modified);


        int carrierIndex = METADATA_TOTAL_BYTES;
        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                byte bitToHide = (byte) ((b >> bit) & 1);


                if (carrierIndex % 3 == 2) {
                    carrierIndex++;
                }

                if (carrierIndex >= modified.length) {
                    throw new RuntimeException("Error de implementación: Capacidad insuficiente durante el bucle de embed.");
                }


                modified[carrierIndex] = setLSB(modified[carrierIndex], bitToHide);
                carrierIndex++;
            }
        }
        return modified;
    }


    private void analyzePayload(byte[] carrierPixelData, byte[] payload, Map<Integer, int[]> counters) {
        int carrierByteIndex = METADATA_TOTAL_BYTES;
        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                if (carrierByteIndex % 3 == 2) {
                    carrierByteIndex++;
                }

                if (carrierByteIndex >= carrierPixelData.length) {
                    throw new RuntimeException("Error de implementación: Capacidad insuficiente durante el análisis.");
                }


                if (((b >> bit) & 1) != (carrierPixelData[carrierByteIndex] & 1)) {
                    counters.get(carrierPixelData[carrierByteIndex] & 6)[1]++; // [1] = con cambio
                } else {
                    counters.get(carrierPixelData[carrierByteIndex] & 6)[0]++; // [0] = sin cambio
                }
                carrierByteIndex++;
            }
        }
    }


    private void obfuscateInversionMap(byte[] modified) {
        int mapBitIndex = 0;
        for (int pattern : PATTERNS) {
            byte bitToHide = (byte) (inversionMap.get(pattern) ? 1 : 0);
            modified[mapBitIndex] = super.setLSB(modified[mapBitIndex], bitToHide);
            mapBitIndex++;
        }
    }

    private void initializeCounters(Map<Integer, int[]> counters) {
        for (int pattern : PATTERNS) {
            counters.put(pattern, new int[2]);
        }
    }

    private void fillInversionMap(Map<Integer, int[]> counters) {
        inversionMap.clear();
        for (int pattern : PATTERNS) {
            int[] aux = counters.get(pattern);
            inversionMap.put(pattern, aux[0] < aux[1]);
        }
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean hasExtension) {
        inversionMap.clear();
        for (int i = 0; i < METADATA_TOTAL_BYTES; i++) {
            int lsb = super.getLSB(carrierPixelData[i]);
            inversionMap.put(PATTERNS[i], (lsb & 1) == 1);
        }

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        int carrierIndex = METADATA_TOTAL_BYTES;

        byte[] payloadSizeInfo = new byte[4];
        for (int i = 0; i < 4; i++) {
            ExtractResult result = extractByteInternal(carrierPixelData, carrierIndex);
            payloadSizeInfo[i] = result.extractedByte;
            carrierIndex = result.nextIndex;
        }
        try {
            outputStream.write(payloadSizeInfo);
        } catch (Exception e) {
            throw new RuntimeException("Error building payload size info", e);
        }

        int payloadLength = convertPayloadLength(payloadSizeInfo);

        for (int i = 0; i < payloadLength; i++) {
            ExtractResult result = extractByteInternal(carrierPixelData, carrierIndex);
            outputStream.write(result.extractedByte);
            carrierIndex = result.nextIndex;
        }

        if (!hasExtension) {
            return outputStream.toByteArray();
        }

        while (carrierIndex < carrierPixelData.length) {
            ExtractResult result = extractByteInternal(carrierPixelData, carrierIndex);
            byte b = result.extractedByte;
            outputStream.write(b);
            carrierIndex = result.nextIndex;
            if (b == '\0') {
                break;
            }
        }

        return outputStream.toByteArray();
    }

    private ExtractResult extractByteInternal(byte[] carrierPixelData, int carrierIndex) {
        byte b = 0;
        for (int bit = 7; bit >= 0; bit--) {
            if (carrierIndex % 3 == 2) {
                carrierIndex++;
            }

            if (carrierIndex >= carrierPixelData.length) {
                throw new RuntimeException("Error extrayendo payload: el portador terminó inesperadamente.");
            }
            int lsb = getLSB(carrierPixelData[carrierIndex]);
            b |= (byte) ((lsb & 1) << bit);
            carrierIndex++;
        }
        return new ExtractResult(b, carrierIndex);
    }



    @Override
    public int getCapacity(byte[] carrierPixelData) {
        int dataBytesAvailable = carrierPixelData.length - METADATA_TOTAL_BYTES;
        int usableBytes = (int) Math.floor(dataBytesAvailable * (2.0 / 3.0));
        return usableBytes / 8;
    }

    @Override
    protected byte setLSB(byte originalByte, byte bitToSet) {
        int key = originalByte & 6;
        if (Boolean.TRUE.equals(inversionMap.get(key))) {
            bitToSet = (byte) (~bitToSet & 1);
        }
        return super.setLSB(originalByte, bitToSet);
    }

    @Override
    protected int getLSB(byte b) {
        int lsb = super.getLSB(b);
        int key = b & 6;
        if (Boolean.TRUE.equals(inversionMap.get(key))) {
            return lsb == 0 ? 1 : 0;
        }
        return lsb;
    }
}