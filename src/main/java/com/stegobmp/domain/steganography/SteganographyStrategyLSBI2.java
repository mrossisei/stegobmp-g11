package com.stegobmp.domain.steganography;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

public class SteganographyStrategyLSBI2 extends SteganographyStrategyAbs{
    private static final int[] PATTERNS = {0, 2, 4, 6};
    private static final int METADATA_TOTAL_BYTES = 4;
    private final Map<Integer, Boolean> inversionMap = new HashMap<>();

    public SteganographyStrategyLSBI2() {
        super(StegAlgorithm.LSBI);
    }

    @Override
    protected byte[] modifyCarrierData(byte[] carrierPixelData, byte[] payload, int carrierByteIndex) {
        Map<Integer, int[]> counters = new LinkedHashMap<>();
        for (int pattern : PATTERNS) {
            counters.put(pattern, new int[2]); // [0] = sin cambio, [1] = con cambio
        }

        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                if (((b >> bit) & 1) != (carrierPixelData[carrierByteIndex] & 1)) {
                    counters.get(carrierPixelData[carrierByteIndex] & 6)[1]++;
                } else {
                    counters.get(carrierPixelData[carrierByteIndex] & 6)[0]++;
                }
                carrierByteIndex++;
            }
        }
        for (int pattern : PATTERNS) {
            int[] aux = counters.get(pattern);
            inversionMap.put(pattern, aux[0] < aux[1]); // [0] = sin cambio, [1] = con cambio
        }

        byte[] modified = carrierPixelData.clone();
        int mapBitIndex = 0;

        //oculto mapa
        for (int pattern : PATTERNS) {
            byte bitToHide = (byte) (inversionMap.get(pattern) ? 1 : 0);
            modified[mapBitIndex] = setLSB(modified[mapBitIndex], bitToHide);
            mapBitIndex++;
        }

        return super.modifyCarrierData(modified, payload, 4);
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean hasExtension) {
        for (int i = 0; i < 4; i++) {
            inversionMap.put(PATTERNS[i], (carrierPixelData[i] & 1) == 1);
        }
        byte[] trimmed = Arrays.copyOfRange(carrierPixelData, METADATA_TOTAL_BYTES, carrierPixelData.length);
        return super.extract(trimmed, hasExtension);
    }

    @Override
    public int getCapacity(byte[] carrierPixelData) {
        return (carrierPixelData.length - METADATA_TOTAL_BYTES) / 8;
    }

    @Override
    protected byte setLSB(byte originalByte, byte bitToSet) {
        int key = originalByte & 6;
        if (inversionMap.get(key)) bitToSet = (byte) (~bitToSet & 1);
        return super.setLSB(originalByte, bitToSet);
    }

    @Override
    protected int getLSB(byte b) {
        int key = b & 6;
        if (inversionMap.get(key)) b = (byte)(~b & 1);
        return super.getLSB(b);
    }
}
