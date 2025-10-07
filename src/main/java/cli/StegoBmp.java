package cli;

import service.StegoBmpService;

/**
 * Punto de entrada de la aplicación.
 * Su única responsabilidad es iniciar el parseo de argumentos
 * y delegar la ejecución a la capa de servicio.
 */
public class StegoBmp {

    public static void main(String[] args) {
        StegoConfig config = CommandLineParser.parse(args);

        StegoBmpService service = new StegoBmpService();

        switch (config.getOperationMode()) {
            case EMBED:
                service.embed(config);
                System.out.println("Información oculta exitosamente.");
                break;
            case EXTRACT:
                service.extract(config);
                System.out.println("Información extraída exitosamente.");
                break;
        }

    }
}
