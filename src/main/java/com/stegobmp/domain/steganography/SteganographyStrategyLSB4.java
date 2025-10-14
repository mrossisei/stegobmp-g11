package com.stegobmp.domain.steganography;

import java.io.ByteArrayOutputStream;

public class SteganographyStrategyLSB4 extends  SteganographyStrategyAbs {

    public SteganographyStrategyLSB4() {
        super(StegAlgorithm.LSB4);
    }

    @Override
    public byte[] embed(byte[] carrierPixelData, byte[] payload) {

        byte[] modifiedPixelData = super.embed(carrierPixelData, payload);

        int carrierByteIndex = 0;
        for (byte b : payload) {
            for (int bit = 7; bit >= 0; bit--) {
                modifiedPixelData[carrierByteIndex] = setLSB(modifiedPixelData[carrierByteIndex], (byte) (b >> bit));
                carrierByteIndex++;
            }
        }

        return modifiedPixelData;
    }

    @Override
    public byte[] extract(byte[] carrierPixelData, boolean withExtension) {
        byte[] payloadSizeInfo = extractPayloadSizeInfo(carrierPixelData); // Los primeros 4 bytes almacenan el largo del payload

        // Convertir Big Endian 4 bytes a int
        int payloadLength = convertPayloadLength(payloadSizeInfo) + PAYLOAD_SIZE_INFO_LENGTH;

        byte[] extractedPayload = extractPayload(carrierPixelData, payloadLength);

        if (!withExtension) {
            return extractedPayload;
        }

        byte[] extensionPayload = extractExtension(carrierPixelData, payloadLength);

        return buildPayloadWithExtension(extractedPayload, extensionPayload);
    }

}
