package cli;

/**
 * Enum para los modos de encriptación soportados.
 */
public enum CryptoMode {
    ECB,
    CFB,
    OFB,
    CBC

    public static CryptoMode fromString(String text) {
        try {
            return CryptoMode.valueOf(text.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new StegoException("Modo de cifrado no válido: '" + text + "'. Use ecb, cfb, ofb o cbc.");
        }
    }
}