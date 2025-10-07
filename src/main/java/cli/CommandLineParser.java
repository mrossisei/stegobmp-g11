package cli;

/**
 * Responsable de parsear los argumentos de la línea de comandos
 * y convertirlos en un objeto de configuración (StegoConfig) fácil de usar.
 * Valida que los argumentos requeridos estén presentes.
 */
public class CommandLineParser {

    /**
     * Parsea el array de argumentos y devuelve un objeto de configuración.
     * @param args Los argumentos de la línea de comandos.
     * @return Un objeto StegoConfig con toda la información parseada.
     * @throws IllegalArgumentException si los argumentos son inválidos o faltan.
     */
    public static StegoConfig parse(String[] args) {
        // Lógica de parseo.
        // Por ejemplo:
        // if (args[0].equals("-embed")) { ... }
        // ...

        // Se debe validar que si -embed está, también lo estén -in, -p, -out, -steg.
        // Se debe validar que si -pass está, también lo esté -a y -m, o usar defaults.

        // ...
        if (args.length < 2) {
            throw new IllegalArgumentException("Argumentos insuficientes. Se requiere -embed o -extract.");
        }

        // lógica

        // Llenar esto con los datos de `args`.
        return new StegoConfig(/* datos parseados */);
    }
}
