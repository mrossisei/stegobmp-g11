package com.stegobmp.domain.steganography;

import com.stegobmp.domain.crypto.CryptoHandler;

import java.io.ByteArrayOutputStream;

public class SteganographyStrategyLSB1 implements  SteganographyStrategy {
    private final CryptoHandler cryptoHandler;

    public SteganographyStrategyLSB1() {
        this.cryptoHandler = null;
    }

    public SteganographyStrategyLSB1(CryptoHandler cryptoHandler) {
        this.cryptoHandler = cryptoHandler;
    }

    // recibe:
    // | size | datos | ext
    // | size | cifrado(size, datos, ext)

    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {
        if (payload.length > getCapacity(carrierPixelData)) {
            throw new IllegalArgumentException("Payload is too large to fit in the carrier image using LSB1.");
        }

        byte[] modifiedPixelData = carrierPixelData.clone();

        int carrierByteIndex = 0;
        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                modifiedPixelData[carrierByteIndex] = setLSB(modifiedPixelData[carrierByteIndex], (byte) (b >> bit));
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

    private byte[] extractPayloadSizeInfo(byte[] carrierPixel) {
        byte[] payloadSizeInfo = new byte[4]; // Los primeros 4 bytes almacenan el largo del payload
        int carrierBitIndex = 0;

        // ---- Extraer los 4 bytes que contienen el tamaño del payload ----
        for (int i = 0; i < 4; i++) {
            byte b = extractByte(carrierPixel, carrierBitIndex);
            payloadSizeInfo[i] = b;
            carrierBitIndex += 8; // Avanza 8 bits por byte
        }
        return payloadSizeInfo;
    }

    private int convertPayloadLength(byte[] payloadSizeInfo) {
        return ((payloadSizeInfo[0] & 0xFF) << 24) |
                ((payloadSizeInfo[1] & 0xFF) << 16) |
                ((payloadSizeInfo[2] & 0xFF) << 8)  |
                (payloadSizeInfo[3] & 0xFF);
    }

    private byte[] extractPayload(byte[] carrierPixelData, int payloadLength) {
        byte[] extractedPayload = new byte[payloadLength];
        int startBitIndex = 0;

        for (int i = 0; i < payloadLength; i++) {
            byte b = extractByte(carrierPixelData, startBitIndex);
            extractedPayload[i] = b;
            startBitIndex += 8; // Avanza 8 bits por byte
        }
        return extractedPayload;
    }

    private byte[] extractDecryptedPayload(byte[] extractedPayload) {
        byte[] decryptedPayload = new byte[extractedPayload.length - 5]; //5 because '\0'
        System.arraycopy(extractedPayload, 0, decryptedPayload, 0, extractedPayload.length - 5);
        return decryptedPayload;
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean withExtension) {
        byte[] payloadSizeInfo = extractPayloadSizeInfo(carrierPixelData); // Los primeros 4 bytes almacenan el largo del payload
        int carrierBitIndex = 0; // header bits

        // Convertir Big Endian 4 bytes a int
        int payloadLength = convertPayloadLength(payloadSizeInfo) + 4;

        byte[] extractedPayload = extractPayload(carrierPixelData, payloadLength);

        if (!withExtension) {
            return extractedPayload;
        }

        carrierBitIndex += payloadLength * 8; // Mover el índice después del payload extraído

        ByteArrayOutputStream extensionStream = new ByteArrayOutputStream();
        while (carrierPixelData.length > carrierBitIndex) {
            byte b = extractByte(carrierPixelData, carrierBitIndex);
            carrierBitIndex += 8;
            extensionStream.write(b);
            if (b == '\0') {
                break;
            }
        }
        byte[] extensionPayload = extensionStream.toByteArray();

        byte[] finalPayload = new byte[payloadLength + extensionPayload.length];
        System.arraycopy(extractedPayload, 0, finalPayload, 0, payloadLength);
        System.arraycopy(extensionPayload, 0, finalPayload, payloadLength, extensionPayload.length);
        extractedPayload = finalPayload;


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
        return (byte) (clearedByte | (bitToSet & 1));
    }

    private int getLSB(byte b) {return b & 1;}
}
