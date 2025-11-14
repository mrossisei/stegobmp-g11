package com.stegobmp.domain.steganography;

public interface SteganographyStrategy {

    byte[] embed(byte[] carrierPixelData, byte[] payload);

    byte[] extract(byte[] carrierPixelData, boolean hasExtension);

    int getCapacity(byte[] carrierPixelData);
}
