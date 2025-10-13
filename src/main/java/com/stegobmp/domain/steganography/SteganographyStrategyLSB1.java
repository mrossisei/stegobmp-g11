package com.stegobmp.domain.steganography;

public class SteganographyStrategyLSB1 implements  SteganographyStrategy {
    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        if (payload.length > getCapacity(carrierPixelData)) {
            throw new IllegalArgumentException("Payload is too large to fit in the carrier image using LSB1.");
        }

        byte[] modifiedPixelData = carrierPixelData.clone();

        int carrierByteIndex = 0;
        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                modifiedPixelData[carrierByteIndex] = setLSB(modifiedPixelData[carrierByteIndex], (byte) ((b >> bit) & 0x01));
                carrierByteIndex++;
            }
        }

        return modifiedPixelData;
    }

    private byte extractByte(byte[] carrierPixelData, int startBitIndex) {
        byte b = 0;
        for (int bit = 7; bit >= 0; bit--) {
            b |= (byte) (getLSB(carrierPixelData[startBitIndex]) << bit);
            startBitIndex++;
        }
        return b;
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean isEncrypted) {
        byte[] payloadSizeInfo = new byte[4]; // The first 4 bytes store the payload length
        int carrierByteIndex = 0;
        for (int i = 0; i < 4; i++) {
            byte b = extractByte(carrierPixelData, carrierByteIndex);
            payloadSizeInfo[i] = b;
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
        }

        if (!isEncrypted) {
            // Continue extraction until \0 is found
            byte[] extensionPayload = new byte[16]; // Assuming max extension length is 20 bytes
            int extIndex = 0;
            while (true) {
                byte b = extractByte(carrierPixelData, carrierByteIndex);
                if (extIndex == payloadLength) {
                    // Extend the array if needed
                    byte[] newExtensionPayload = new byte[extensionPayload.length * 2];
                    System.arraycopy(extensionPayload, 0, newExtensionPayload, 0, extensionPayload.length);
                    extensionPayload = newExtensionPayload;
                }
                extensionPayload[extIndex++] = b;
                if (b =='\0') {
                    break;
                }
            }
            byte[] finalPayload = new byte[payloadLength + extIndex];
            System.arraycopy(extractedPayload, 0, finalPayload, 0, payloadLength);
            System.arraycopy(extensionPayload, 0, finalPayload, payloadLength, extIndex);
            extractedPayload = finalPayload;
        }

        return extractedPayload;
    }

    @Override
    public int getCapacity(byte[] carrierPixelData) {
        return (int) Math.ceil(carrierPixelData.length / 8.0);
    }

    private byte setLSB(byte originalByte, byte bitToSet) {
        // Clear the least significant bit (LSB) of the original byte
        byte clearedByte = (byte) (originalByte & 0xFE);
        // Set the LSB to the new bit
        return (byte) (clearedByte | (bitToSet & 0x01));
    }

    private byte getLSB(byte originalByte) {
        return (byte) (originalByte & 0x01);
    }
}
