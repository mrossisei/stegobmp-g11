package com.stegobmp.domain.steganography;

import java.io.ByteArrayOutputStream;

public class SteganographyStrategyLSB4 implements  SteganographyStrategy {
    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        if (payload.length > getCapacity(carrierPixelData)) {
            throw new IllegalArgumentException("Payload is too large to fit in the carrier image using LSB4.");
        }

        byte[] modifiedPixelData = carrierPixelData.clone();

        int carrierByteIndex = 0;
        for (byte b : payload) {
            for (int bit = 1; bit >= 0; bit--) {
                modifiedPixelData[carrierByteIndex] = setLSB4(modifiedPixelData[carrierByteIndex], (byte) ((b >> (bit*4)) & 0x0F));
                carrierByteIndex++;
            }
        }

        return modifiedPixelData;
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean isEncrypted) {
        byte[] payloadSizeInfo = new byte[4]; // The first 4 bytes store the payload length
        int carrierByteIndex = 0;
        for (int i = 0; i < 4; i++) {
            byte b = extractByte(carrierPixelData, carrierByteIndex);
            payloadSizeInfo[i] = b;
            carrierByteIndex += 2; // Move 2 bytes for each byte extracted
        }

        // Convert Big Endian 4 bytes to int
        int payloadLength = ((payloadSizeInfo[0] & 0xFF) << 24) |
                ((payloadSizeInfo[1] & 0xFF) << 16) |
                ((payloadSizeInfo[2] & 0xFF) << 8)  |
                (payloadSizeInfo[3] & 0xFF);

        byte[] extractedPayload = new byte[payloadLength];

        for (int i = 0; i < payloadLength; i++) {
            byte b = extractByte(carrierPixelData, carrierByteIndex);
            extractedPayload[i] = b;
            carrierByteIndex += 2; // Move 2 bytes for each byte extracted
        }

        if (!isEncrypted) {
            // Extract extension
            ByteArrayOutputStream extStream = new ByteArrayOutputStream();
            while (true) {
                byte b = extractByte(carrierPixelData, carrierByteIndex);
                carrierByteIndex += 2;
                extStream.write(b);
                if (b == 0) break;
            }
            byte[] extensionPayload = extStream.toByteArray();
            int extLen = extensionPayload.length;
            if (extLen > 0 && extensionPayload[extLen - 1] == 0) {
                extLen--; // Remove null terminator from length, it will blow up when writing to file otherwise
            }
            byte[] finalPayload = new byte[payloadLength + extLen];
            System.arraycopy(extractedPayload, 0, finalPayload, 0, payloadLength);
            System.arraycopy(extensionPayload, 0, finalPayload, payloadLength, extLen);
            extractedPayload = finalPayload;
        }

        return extractedPayload;
    }

    @Override
    public int getCapacity(byte[] carrierPixelData) {
        return (int) Math.ceil(carrierPixelData.length / 2.0);
    }

    private byte extractByte(byte[] carrierPixelData, int startBitIndex) {
        byte b = 0;
        for (int bit = 1; bit >= 0; bit--) {
            b |= (byte) (getLSB4(carrierPixelData[startBitIndex]) << (bit * 4));
            startBitIndex++;
        }
        return b;
    }

    private byte getLSB4(byte b) {
        return (byte) (b & 0x0F);
    }

    private byte setLSB4(byte originalByte, byte bitsToSet) {
        // Clear the least significant bit (LSB) of the original byte
        byte clearedByte = (byte) (originalByte & 0xF0);
        // Set the LSB to the new bit
        return (byte) (clearedByte | (bitsToSet & 0x0F));
    }

}
