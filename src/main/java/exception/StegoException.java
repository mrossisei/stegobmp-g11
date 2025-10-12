package exception;

/**
 * Excepción personalizada para errores relacionados con esteganografía.
 * Se lanza cuando ocurren errores específicos del dominio, como:
 * - Capacidad insuficiente en el archivo portador
 * - Formato de archivo BMP inválido
 * - Errores en encriptación/desencriptación
 */
public class StegoException extends Exception {

    public StegoException(String message) {
        super(message);
    }

    public StegoException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * Crea una excepción para indicar capacidad insuficiente.
     * @param required Bytes requeridos para ocultar.
     * @param available Bytes disponibles en el portador.
     * @return Una StegoException con mensaje formateado.
     */
    public static StegoException insufficientCapacity(int required, int available) {
        return new StegoException(String.format(
            "Capacidad insuficiente: se requieren %d bytes pero solo hay %d disponibles",
            required, available
        ));
    }

    /**
     * Crea una excepción para indicar formato BMP inválido.
     * @param reason La razón por la cual el BMP es inválido.
     * @return Una StegoException con mensaje formateado.
     */
    public static StegoException invalidBmpFormat(String reason) {
        return new StegoException("Formato BMP inválido: " + reason);
    }
}