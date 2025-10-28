package com.stegobmp.domain.crypto;

import java.security.spec.KeySpec;
import java.util.Arrays;
import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Handler (manejador) criptográfico que implementa la lógica
 * requerida por el Trabajo Práctico (TP).
 *
 * CORREGIDO PARA USAR:
 * 1. PBKDF2 para derivar Key + IV (según TP [cite: 179, 186])
 * 2. SALT Fijo de 8 bytes en 0 (según TP )
 * 3. Padding correcto (NoPadding para CFB/OFB) (según TP )
 */
public class CryptoHandler {

    private final CryptoConfig cryptoConfig;

    // Constantes de PBKDF2 tomadas del TP y CryptoUtils [cite: 179, 260, 261]
    private static final byte[] FIXED_SALT = {0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00};
    private static final int PBKDF2_ITER = 10000;

    // Clase interna simple para transportar el material criptográfico derivado
    private static class DerivedCryptoMaterial {
        final SecretKey key;
        final IvParameterSpec ivSpec;

        DerivedCryptoMaterial(SecretKey key, IvParameterSpec ivSpec) {
            this.key = key;
            this.ivSpec = ivSpec;
        }
    }

    public CryptoHandler(CryptoConfig cryptoConfig) {
        this.cryptoConfig = cryptoConfig;
    }

    /**
     * Obtiene el nombre del algoritmo JCE ("AES" o "DESede")
     */
    private String getAlgorithmName() {
        // Asumo que CryptoAlgorithm.name() retorna "AES128", "AES192", "3DES", etc.
        String name = cryptoConfig.cryptoAlgorithm().name();
        if (name.startsWith("AES")) {
            return "AES";
        }
        // El TP especifica 3DES (Triple DES) [cite: 185]
        // En JCE, 3DES se llama "DESede"
        return "DESede";
    }

    /**
     * Deriva la Clave (Key) y el Vector de Inicialización (IV) usando PBKDF2,
     * como se especifica en el TP [cite: 179, 186] y en CryptoUtils [cite: 262-266].
     */
    private DerivedCryptoMaterial deriveKeyAndIv() throws Exception {
        String jceAlgorithmName = getAlgorithmName();
        String password = cryptoConfig.password();

        // 1. Determinar longitudes de Key e IV
        // Esta lógica se basa en el CryptoHandler original,
        // ya que depende del enum CryptoAlgorithm que no tengo.
        int keyLengthBytes;
        if (jceAlgorithmName.equals("AES")) {
            keyLengthBytes = switch (cryptoConfig.cryptoAlgorithm()) {
                case AES128 -> 16; // 128 bits
                case AES192 -> 24; // 192 bits
                case AES256 -> 32; // 256 bits
                default -> throw new IllegalArgumentException("Algoritmo AES no soportado");
            };
        } else { // DESede (3DES) [cite: 185]
            keyLengthBytes = 24; // 192 bits (3 * 64 bits, aunque 3*56 efectivos)
        }

        // AES usa bloques de 16 bytes (128 bits). 3DES usa 8 bytes (64 bits).
        int ivLengthBytes = jceAlgorithmName.equals("AES") ? 16 : 8;

        // 2. Lógica PBKDF2 (tomada de CryptoUtils [cite: 262-264])
        int keyLengthBits = keyLengthBytes * 8;
        int ivLengthBits = ivLengthBytes * 8;
        int totalBits = keyLengthBits + ivLengthBits;

        SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
        KeySpec spec = new PBEKeySpec(password.toCharArray(), FIXED_SALT, PBKDF2_ITER, totalBits);
        SecretKey tmp = factory.generateSecret(spec);
        byte[] derivedMaterial = tmp.getEncoded();

        // 3. Separar Key e IV del material derivado [cite: 264]
        byte[] keyBytes = Arrays.copyOfRange(derivedMaterial, 0, keyLengthBytes);
        byte[] ivBytes = Arrays.copyOfRange(derivedMaterial, keyLengthBytes, derivedMaterial.length);

        // 4. Construir los objetos SecretKey e IvParameterSpec [cite: 265, 266]
        SecretKey secretKey = new SecretKeySpec(keyBytes, jceAlgorithmName);
        IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);

        return new DerivedCryptoMaterial(secretKey, ivSpec);
    }

    /**
     * Obtiene el string de transformación JCE (ej: "AES/CBC/PKCS5Padding")
     * respetando el padding de los modos CFB/OFB. [cite: 274-277]
     */
    private String getTransformation() {
        String algorithm = getAlgorithmName();
        CryptoMode mode = cryptoConfig.cryptoMode();
        String padding = "PKCS5Padding"; // Padding por defecto [cite: 176]
        String modeName = mode.name();
        // El TP pide NoPadding para CFB y OFB
        if (mode == CryptoMode.CFB || mode == CryptoMode.OFB) {
            padding = "NoPadding";
        }
        if (mode == CryptoMode.CFB) {
            modeName = "CFB8";
        }
        // Nota: El TP [cite: 184] pide 8 bits para CFB.
        // Tu enum CryptoMode.java solo define "CFB", no "CFB8".
        // Esta implementación, al igual que CryptoUtils, usará "CFB" (feedback de 128 bits)[cite: 188].
        return algorithm + "/" + modeName + "/" + padding;
    }

    /**
     * Encripta los datos.
     * Ya no adjunta el IV; este se deriva de la password.
     */
    public byte[] encrypt(byte[] data) {
        try {
            // 1. Derivar Key e IV usando PBKDF2
            DerivedCryptoMaterial material = deriveKeyAndIv();

            // 2. Obtener transformación (ej: "AES/CFB/NoPadding") [cite: 274]
            String transformation = getTransformation();
            Cipher cipher = Cipher.getInstance(transformation);

            // 3. Inicializar Cipher (lógica de CryptoUtils [cite: 267-269])
            if (cryptoConfig.cryptoMode() == CryptoMode.ECB) {
                cipher.init(Cipher.ENCRYPT_MODE, material.key); // [cite: 268]
            } else {
                cipher.init(Cipher.ENCRYPT_MODE, material.key, material.ivSpec); // [cite: 267]
            }

            // 4. Encriptar [cite: 269]
            return cipher.doFinal(data);

        } catch (Exception e) {
            throw new RuntimeException("Fallo la encriptación", e);
        }
    }

    /**
     * Desencripta los datos.
     * Ya no extrae el IV del principio; este se deriva de la password.
     */
    public byte[] decrypt(byte[] data) {
        try {
            // 1. Derivar Key e IV usando PBKDF2
            DerivedCryptoMaterial material = deriveKeyAndIv();

            // 2. Obtener transformación [cite: 274]
            String transformation = getTransformation();
            Cipher cipher = Cipher.getInstance(transformation);

            // 3. Inicializar Cipher (lógica de CryptoUtils [cite: 271-273])
            if (cryptoConfig.cryptoMode() == CryptoMode.ECB) {
                cipher.init(Cipher.DECRYPT_MODE, material.key); // [cite: 272]
            } else {
                cipher.init(Cipher.DECRYPT_MODE, material.key, material.ivSpec); // [cite: 271]
            }

            // 4. Desencriptar [cite: 273]
            return cipher.doFinal(data);

        } catch (Exception e) {
            throw new RuntimeException("Fallo la desencriptación", e);
        }
    }
}