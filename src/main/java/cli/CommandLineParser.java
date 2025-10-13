package cli;

import exception.StegoException;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Parsea los argumentos de la línea de comandos de forma robusta y los convierte
 * en un objeto CliConfig fuertemente tipado.
 * Realiza todas las validaciones lógicas y de dependencias entre parámetros.
 */
public class CommandLineParser {

    //Parsea el array de argumentos y devuelve un objeto de configuración.
    public static CliConfig parse(String[] args) {
        if (args.length == 0) {
            throw new StegoException("No se proporcionaron argumentos. Uso: stegobmp -embed | -extract ...");
        }

        Map<String, String> argMap = new HashMap<>();
        for (int i = 0; i < args.length; i++) {
            if (args[i].startsWith("-")) {
                // Es un flag, si el siguiente argumento no es un flag, es su valor.
                if (i + 1 < args.length && !args[i + 1].startsWith("-")) {
                    argMap.put(args[i], args[i + 1]);
                    i++; // Saltar el valor ya que lo hemos consumido
                } else {
                    // Es un flag sin valor (como -embed o -extract)
                    argMap.put(args[i], null);
                }
            }
        }

        OperationMode operationMode = parseOperationMode(argMap);

        // Parámetros obligatorios para ambas operaciones
        String carrierFile = getRequiredArg(argMap, "-p");
        String outputFile = getRequiredArg(argMap, "-out");
        StegAlgorithm stegAlgorithm = StegAlgorithm.fromString(getRequiredArg(argMap, "-steg"));

        // Parámetro obligatorio solo para -embed
        String inputFile = null;
        if (operationMode == OperationMode.EMBED) {
            inputFile = getRequiredArg(argMap, "-in");
        }

        // Parámetros opcionales de criptografía
        Optional<String> password = Optional.ofNullable(argMap.get("-pass"));
        Optional<CryptoAlgorithm> cryptoAlgorithm = Optional.ofNullable(argMap.get("-a")).map(CryptoAlgorithm::fromString);
        Optional<CryptoMode> cryptoMode = Optional.ofNullable(argMap.get("-m")).map(CryptoMode::fromString);

        // Validar lógica de criptografía
        if ((cryptoAlgorithm.isPresent() || cryptoMode.isPresent()) && password.isEmpty()) {
            throw new StegoException("Se especificó un algoritmo/modo de cifrado pero falta el parámetro -pass <password>.");
        }

        // Aplicar valores por defecto si solo se provee la password
        if (password.isPresent()) {
            if (cryptoAlgorithm.isEmpty()) {
                cryptoAlgorithm = Optional.of(CryptoAlgorithm.AES128); // Default
            }
            if (cryptoMode.isEmpty()) {
                cryptoMode = Optional.of(CryptoMode.CBC); // Default
            }
        }

        return new CliConfig(
                operationMode,
                inputFile,
                carrierFile,
                outputFile,
                stegAlgorithm,
                cryptoAlgorithm,
                cryptoMode,
                password
        );
    }

    private static OperationMode parseOperationMode(Map<String, String> argMap) {
        boolean hasEmbed = argMap.containsKey("-embed");
        boolean hasExtract = argMap.containsKey("-extract");

        if (hasEmbed && hasExtract) {
            throw new StegoException("No se puede especificar -embed y -extract al mismo tiempo.");
        }
        if (!hasEmbed && !hasExtract) {
            throw new StegoException("Se debe especificar la operación: -embed o -extract.");
        }
        return hasEmbed ? OperationMode.EMBED : OperationMode.EXTRACT;
    }

    private static String getRequiredArg(Map<String, String> argMap, String key) {
        String value = argMap.get(key);
        if (value == null) {
            throw new StegoException("Falta el parámetro obligatorio: " + key);
        }
        return value;
    }
}