package com.stegobmp.domain.payload;

import com.stegobmp.domain.crypto.CryptoConfig;
import com.stegobmp.domain.crypto.CryptoHandler;
import com.stegobmp.service.StegoBmpService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Responsable de la construcción y deconstrucción del payload.
 * Encapsula la lógica de cómo se empaquetan los datos (tamaño, datos crudos, extensión)
 * antes de la esteganografía y cómo se desempaquetan después.
 * También interactúa con el CryptoHandler si se requiere cifrado.
 */
public class PayloadHandler {

    private static final int SIZE_BYTES = 4; // Tamaño de un entero en bytes.

    // Prepara el payload final que será ocultado: Construye la estructura de datos, la cifra si es necesario, y añade los metadatos de tamaño.
    // plaintext = [Tamaño real (4 bytes)] || [Datos del archivo] || [Extensión (con '.' y '\0')]
    // (opcional) cifrado = [Tamaño del cifrado (4 bytes)] || [cifrado(plaintext)]
    public byte[] preparePayload(byte[] secretFileData, String fileName, Optional<CryptoHandler> cryptoHandler) throws IOException {
        byte[] plaintextPayload = buildPlaintextPayload(secretFileData, fileName);

        if (cryptoHandler.isPresent()) {
            byte[] encryptedData = cryptoHandler.get().encrypt(plaintextPayload);
            return buildFinalEncryptedPayload(encryptedData);
        } else {
            return plaintextPayload;
        }
    }

    // Camino Inverso: Parsea el payload extraído del archivo portador para recuperar el archivo original.
//    public ExtractedFile parsePayload(byte[] extractedPayload, Optional<CryptoHandler> cryptoHandler) {
//       // implementar
//    }


    // --- MÉTODOS PRIVADOS ---

    private byte[] buildPlaintextPayload(byte[] data, String fileName) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();

        stream.write(ByteBuffer.allocate(SIZE_BYTES).putInt(data.length).array()); // Tamaño del archivo (4 bytes, Big Endian).

        stream.write(data); // TODO: ¿Acá hay que rellenar (padding)? Creo que no, pero por si acaso dejo el comment para revisar

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
            return "."; // Si no hay extensión o el punto está al final, devolvemos solo el punto.
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