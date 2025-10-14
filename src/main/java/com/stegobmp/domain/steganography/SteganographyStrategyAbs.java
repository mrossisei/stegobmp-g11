package com.stegobmp.domain.steganography;

import java.io.ByteArrayOutputStream;

public abstract class SteganographyStrategyAbs implements SteganographyStrategy {

    private final StegAlgorithm stegAlgorithm;
    private final int byteRatio;
    private final int bitsPerByte;
    protected final static int PAYLOAD_SIZE_INFO_LENGTH = 4;

    public SteganographyStrategyAbs(StegAlgorithm stegAlgorithm) {
        this.stegAlgorithm = stegAlgorithm;
        bitsPerByte = switch (stegAlgorithm) {
            case LSB1, LSBI -> 1;
            case LSB4 -> 4;
        };
        byteRatio = 8 / bitsPerByte;
    }


    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        if (payload.length > getCapacity(carrierPixelData)) {
            throw new IllegalArgumentException("Payload is too large to fit in the carrier image using " + stegAlgorithm.name());
        }

        return carrierPixelData.clone();
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean hasExtension) {
        return new byte[0];
    }

    @Override
    public int getCapacity(byte[] carrierPixelData) {
        return (int) Math.ceil( carrierPixelData.length / (double) byteRatio );
    }




    protected byte extractByte(byte[] carrierPixelData, int startBitIndex) {
        byte b = 0;
        for (int bit = byteRatio-1; bit >= 0; bit--) {
            b |= (byte) (getLSB(carrierPixelData[startBitIndex]) << (bit*bitsPerByte));
            startBitIndex++;
        }
        return b;
    }

    protected byte[] extractPayload(byte[] carrierPixelData, int payloadLength) {
        byte[] extractedPayload = new byte[payloadLength];
        int startBitIndex = 0;

        for (int i = 0; i < payloadLength; i++) {
            byte b = extractByte(carrierPixelData, startBitIndex);
            extractedPayload[i] = b;
            startBitIndex += byteRatio;
        }
        return extractedPayload;
    }

    protected int convertPayloadLength(byte[] payloadSizeInfo) {
        return ((payloadSizeInfo[0] & 0xFF) << 24) |
                ((payloadSizeInfo[1] & 0xFF) << 16) |
                ((payloadSizeInfo[2] & 0xFF) << 8)  |
                (payloadSizeInfo[3] & 0xFF);
    }

    protected byte[] extractPayloadSizeInfo(byte[] carrierPixel) {
        byte[] payloadSizeInfo = new byte[4];
        int carrierBitIndex = 0;

        for (int i = 0; i < 4; i++) {
            byte b = extractByte(carrierPixel, carrierBitIndex);
            payloadSizeInfo[i] = b;
            carrierBitIndex += byteRatio;
        }
        return payloadSizeInfo;
    }

    protected byte[] extractExtension(byte[] carrierPixelData, int startBitIndex) {
        startBitIndex = startBitIndex * byteRatio;
        ByteArrayOutputStream extensionStream = new ByteArrayOutputStream();
        while (carrierPixelData.length > startBitIndex) {
            byte b = extractByte(carrierPixelData, startBitIndex);
            startBitIndex += byteRatio;
            extensionStream.write(b);
            if (b == '\0') {
                break;
            }
        }
        return extensionStream.toByteArray();
    }

    protected byte[] buildPayloadWithExtension(byte[] payload, byte[] extension) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(payload);
            outputStream.write(extension);
//            outputStream.write(0); // TODO should i put the null terminator to be consistent?
        } catch (Exception e) {
            throw new RuntimeException("Error building payload with extension", e);
        }
        return outputStream.toByteArray();
    }

    protected byte setLSB(byte originalByte, byte bitToSet) {
        int bitsToClear = bitsPerByte == 1 ? 0xFE : 0xF0;
        byte clearedByte = (byte) (originalByte & bitsToClear);
        return (byte) (clearedByte | (bitToSet & bitsPerByte));
    }

    protected int getLSB(byte b) {return b & bitsPerByte;}

}
