package cli;

import service.StegoBmpService;
import service.ServiceConfig;
import exception.StegoException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * Clase principal y punto de entrada de la aplicación.
 * Responsabilidades:
 * 1. Parsear los argumentos de la línea de comandos.
 * 2. Manejar la I/O de archivos (leer los archivos de entrada).
 * 3. Delegar la ejecución a la capa de servicio con los datos ya cargados.
 * 4. Manejar las excepciones y presentar los errores al usuario.
 */
public class StegoBmp {

    public static void main(String[] args) {
        CliConfig cliConfig = CommandLineParser.parse(args);

        byte[] carrierData = readFileBytes(cliConfig.carrierFile());
        byte[] secretData = null;
        if (cliConfig.operationMode() == OperationMode.EMBED) {
            secretData = readFileBytes(cliConfig.inputFile());
        }

        ServiceConfig serviceConfig = new ServiceConfig(
                carrierData,
                secretData,
                cliConfig.inputFile(), // Pasamos el nombre para la extensión
                cliConfig.stegAlgorithm(),
                cliConfig.cryptoAlgorithm(),
                cliConfig.cryptoMode(),
                cliConfig.password()
        );

        StegoBmpService service = new StegoBmpService();

        if (cliConfig.operationMode() == OperationMode.EMBED) {
            byte[] outputFileBytes = service.embed(serviceConfig);
            writeFileBytes(cliConfig.outputFile(), outputFileBytes);
        } else { // EXTRACT
            byte[] extractedFileBytes = service.extract(serviceConfig);
            String outputFileName = cliConfig.outputFile() + service.getLastExtractedFileExtension();
            writeFileBytes(outputFileName, extractedFileBytes);
        }



    }


    private static byte[] readFileBytes(String path) throws IOException {
        return Files.readAllBytes(Paths.get(path));
    }

    private static void writeFileBytes(String path, byte[] data) throws IOException {
        Files.write(Paths.get(path), data);
    }
}


