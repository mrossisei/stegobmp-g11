package com.stegobmp.domain.steganography;

public class SteganographyStrategyLSB4 implements  SteganographyStrategy {
    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        return new byte[0];
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean isEncrypted){
        return new byte[0];
    }

    @Override
    public int getCapacity(byte[] carrierPixelData) {
        return (int) Math.ceil(carrierPixelData.length / 2.0);
    }
}
