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
        byte[] payloadSizeInfo = new byte[4]; // Los primeros 4 bytes almacenan el largo del payload
        int carrierBitIndex = 0; // índice en bits, no en bytes

        // ---- Extraer los 4 bytes que contienen el tamaño del payload ----
        for (int i = 0; i < 4; i++) {
            byte b = extractByte(carrierPixelData, carrierBitIndex);
            payloadSizeInfo[i] = b;
            carrierBitIndex += 8; // Avanza 8 bits por byte
        }

        // Convertir Big Endian 4 bytes a int
        int payloadLength = ((payloadSizeInfo[0] & 0xFF) << 24) |
                ((payloadSizeInfo[1] & 0xFF) << 16) |
                ((payloadSizeInfo[2] & 0xFF) << 8)  |
                (payloadSizeInfo[3] & 0xFF);


        // ---- Extraer el payload ----
        byte[] extractedPayload = new byte[payloadLength];

        for (int i = 0; i < payloadLength; i++) {
            byte b = extractByte(carrierPixelData, carrierBitIndex);
            extractedPayload[i] = b;
            carrierBitIndex += 8; // Avanza 8 bits por byte
        }

        // ---- Si no está encriptado, continuar leyendo hasta encontrar '\0' ----
        if (!isEncrypted) {
            byte[] extensionPayload = new byte[16];
            int extIndex = 0;

            while (true) {
                byte b = extractByte(carrierPixelData, carrierBitIndex);
                carrierBitIndex += 8; // Avanza también aquí
                if (extIndex == extensionPayload.length) {
                    // Ampliar el buffer si es necesario
                    byte[] newExtensionPayload = new byte[extensionPayload.length * 2];
                    System.arraycopy(extensionPayload, 0, newExtensionPayload, 0, extensionPayload.length);
                    extensionPayload = newExtensionPayload;
                }
                if (b == '\0') {
                    break;
                }
                extensionPayload[extIndex++] = b;
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
        return (byte) (clearedByte | (bitToSet & 0x01)); //TODO this is unnecessary, & is done prior
    }

    private int getLSB(byte b) {return b & 1;}
}
