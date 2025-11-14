package com.stegobmp.domain.payload;

import com.stegobmp.domain.crypto.CryptoHandler;
import com.stegobmp.service.StegoBmpService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

public class PayloadHandler {

    private static final int SIZE_BYTES = 4;

    public byte[] preparePayload(byte[] secretFileData, String fileName, CryptoHandler cryptoHandler) throws IOException {
        byte[] plaintextPayload = buildPlaintextPayload(secretFileData, fileName);

        if (cryptoHandler != null) {
            byte[] encryptedData = cryptoHandler.encrypt(plaintextPayload);
            return buildFinalEncryptedPayload(encryptedData);
        } else {
            return plaintextPayload;
        }
    }

    public ExtractedFile parsePayload(byte[] payload, CryptoHandler cryptoHandler) {
        if(cryptoHandler == null){
            return parsePlaintextPayload(payload);
        }
        byte[] encryptedPayload = new byte[payload.length - 4];
        System.arraycopy(payload, 4, encryptedPayload, 0, payload.length - 4);
        byte[] decryptedPayload = cryptoHandler.decrypt(encryptedPayload);
        return parsePlaintextPayload(decryptedPayload);

    }


    private ExtractedFile parsePlaintextPayload(byte[] plaintextPayload) {
        byte[] sizeInBytes = Arrays.copyOfRange(plaintextPayload, 0, SIZE_BYTES);
        int size = convertPayloadLength(sizeInBytes);

        byte[] data = Arrays.copyOfRange(plaintextPayload, SIZE_BYTES, SIZE_BYTES + size);

        byte[] extensionBytes = Arrays.copyOfRange(plaintextPayload, SIZE_BYTES + size, plaintextPayload.length - 1); // Excluye el null terminator

        return new ExtractedFile(data, new String(extensionBytes, StandardCharsets.UTF_8));
    }


    private int convertPayloadLength(byte[] payloadSizeInfo) {
        return ((payloadSizeInfo[0] & 0xFF) << 24) |
                ((payloadSizeInfo[1] & 0xFF) << 16) |
                ((payloadSizeInfo[2] & 0xFF) << 8)  |
                (payloadSizeInfo[3] & 0xFF);
    }


    private byte[] buildPlaintextPayload(byte[] data, String fileName) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(ByteBuffer.allocate(SIZE_BYTES).putInt(data.length).array());

        stream.write(data);

        String extension = getFileExtension(fileName);
        stream.write(extension.getBytes(StandardCharsets.UTF_8));
        stream.write('\0');

        return stream.toByteArray();
    }

    private byte[] buildFinalEncryptedPayload(byte[] encryptedData) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(ByteBuffer.allocate(SIZE_BYTES).putInt(encryptedData.length).array());

        stream.write(encryptedData);

        return stream.toByteArray();
    }

    private String getFileExtension(String fileName) {
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == fileName.length() - 1) {
            return ".";
        }
        return fileName.substring(lastDotIndex);
    }
    public StegoBmpService.ExtractedData extractFileExtension(byte[] data) {
        int dotIndex = -1;
        for (int i = data.length - 1; i >= 0; i--) {
            if (data[i] == '.') {
                dotIndex = i;
                break;
            }
        }
        if (dotIndex == -1) {
            return new StegoBmpService.ExtractedData(data, "");
        }

        String extension = new String(data, dotIndex, data.length - dotIndex, StandardCharsets.UTF_8);
        byte[] newData = new byte[dotIndex];
        System.arraycopy(data, 0, newData, 0, dotIndex);
        return new StegoBmpService.ExtractedData(newData, extension);
    }
}