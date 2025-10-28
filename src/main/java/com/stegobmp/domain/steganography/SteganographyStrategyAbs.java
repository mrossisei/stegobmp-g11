package com.stegobmp.domain.steganography;

import java.io.ByteArrayOutputStream;

public abstract class SteganographyStrategyAbs implements SteganographyStrategy {

    private final StegAlgorithm stegAlgorithm;
    protected final int byteRatio;
    protected final int bitsPerByte;
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
        return modifyCarrierData(carrierPixelData, payload, 0);
    }

    protected byte[] modifyCarrierData(byte[] carrierPixelData, byte[] payload, int carrierByteIndex) {
        byte[] modifiedPixelData = carrierPixelData.clone();

        for (byte b : payload) {
            for (int bit = byteRatio-1; bit >= 0; bit--) {
                modifiedPixelData[carrierByteIndex] = setLSB(modifiedPixelData[carrierByteIndex], (byte) (b >> bit * bitsPerByte));
                carrierByteIndex++;
            }
        }
        return modifiedPixelData;
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean hasExtension) {
        byte[] payloadSizeInfo = extractPayloadSizeInfo(carrierPixelData); // Los primeros 4 bytes almacenan el largo del payload

        // Convertir Big Endian 4 bytes a int
        int payloadLength = convertPayloadLength(payloadSizeInfo);
        int dataStartBitIndex = PAYLOAD_SIZE_INFO_LENGTH * byteRatio;
        byte[] extractedData = extractPayload(carrierPixelData, payloadLength, dataStartBitIndex);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        try {
            outputStream.write(payloadSizeInfo);
            outputStream.write(extractedData);
        } catch (Exception e) {
            throw new RuntimeException("Error building payload", e);
        }

        if (!hasExtension) {
            return outputStream.toByteArray();
        }

        int extStartBitIndex = dataStartBitIndex + (payloadLength * byteRatio);


        byte[] extensionPayload = extractExtension(carrierPixelData, extStartBitIndex);


        try {
            outputStream.write(extensionPayload);
        } catch (Exception e) {
            throw new RuntimeException("Error building payload extension", e);
        }

        return outputStream.toByteArray();
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

    protected byte[] extractPayload(byte[] carrierPixelData, int payloadLength, int startBitIndex) {
        byte[] extractedPayload = new byte[payloadLength];

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
        return (byte) (clearedByte | (bitToSet & ~bitsToClear));
    }

    protected int getLSB(byte b) {return b & (bitsPerByte == 1 ? 1 : 0x0F);}

}
