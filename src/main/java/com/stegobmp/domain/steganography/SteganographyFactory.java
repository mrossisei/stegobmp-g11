package com.stegobmp.domain.steganography;

public class SteganographyFactory {


    public static SteganographyStrategy getStrategy(StegAlgorithm algorithm) {
        // TODO
        return new SteganographyStrategy() {
            @Override
            public byte[] embed(byte[] carrierPixelData, byte[] payload) {
                return new byte[0];
            }

            @Override
            public byte[] extract(byte[] carrierPixelData) {
                return new byte[0];
            }

            @Override
            public int getCapacity(byte[] carrierPixelData) {
                return 0;
            }
        };
    }

}
