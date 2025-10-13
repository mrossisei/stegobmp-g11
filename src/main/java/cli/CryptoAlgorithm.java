package cli;

public enum CryptoAlgorithm {
    AES128,
    AES192,
    AES256,
    DES3

    public static CryptoAlgorithm fromString(String text) {
        if ("3des".equalsIgnoreCase(text)) {
            return DES3;
        }
        try {
            return CryptoAlgorithm.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StegoException("Algoritmo de cifrado no válido: '" + text + "'. Use aes128, aes192, aes256 o 3des.");
        }
    }
}