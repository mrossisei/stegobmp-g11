package service;

import cli.StegoConfig;
import domain.bmp.BmpHandler;
import domain.bmp.BmpImage;
import domain.steganography.SteganographyStrategy;

/**
 * Orquestador principal de la aplicación.
 * Coordina las llamadas a los componentes del dominio (handlers, strategies, etc.).
 */
public class StegoBmpService {

    // private final BmpHandler bmpHandler;


    public StegoBmpService() {
        // this.bmpHandler = new BmpHandler();

    }


    public void embed(StegoConfig config) {
        // 1. Leer archivo portador y archivo a ocultar.
        BmpImage carrierImage = bmpHandler.readBmp(config.carrierFile());
        // byte[] secretData = ... leer de config.inputFile() ...

        // 2. Preparar el payload (encriptar si es necesario, agregar tamaño, etc.).
        // PayloadHandler payloadHandler = new PayloadHandler();
        // byte[] payloadToEmbed = payloadHandler.prepareForEmbedding(...);

        // 3. Seleccionar la estrategia de esteganografía.
        // SteganographyStrategy strategy = SteganographyFactory.getStrategy(config.stegAlgorithm());

        // 4. Validar capacidad.
        // if (strategy.getCapacity(carrierImage.pixelData()) < payloadToEmbed.length) {
        //     throw new StegoException("El archivo portador no tiene suficiente capacidad.");
        // }

        // 5. Ocultar la información.
        // byte[] newPixelData = strategy.embed(carrierImage.pixelData(), payloadToEmbed);

        // 6. Escribir el nuevo archivo BMP.
        // BmpImage outputImage = new BmpImage(carrierImage.header(), newPixelData);
        // bmpHandler.writeBmp(config.outputFile(), outputImage);
    }

    /**
     * Orquesta el proceso de extraer información.
     * @param config La configuración de la operación.
     */
    public void extract(StegoConfig config) {
        // Lógica inversa a embed:
        // 1. Leer portador.
        // 2. Seleccionar estrategia.
        // 3. Extraer payload.
        // 4. Procesar payload (desencriptar si es necesario, parsear tamaño).
        // 5. Escribir archivo de salida.
    }
}
