package service;

import domain.bmp.BmpHandler;
import domain.bmp.BmpImage;
import domain.payload.PayloadHandler;
import domain.steganography.SteganographyStrategy;
import exception.StegoException;

/**
 * Orquestador principal de la aplicación.
 * Coordina las llamadas a los componentes del dominio (handlers, strategies, etc.).
 */
public class StegoBmpService {

    private final BmpHandler bmpHandler;
    private final PayloadHandler payloadHandler;

    public StegoBmpService() {
        this.bmpHandler = new BmpHandler();
        this.payloadHandler = new PayloadHandler();
    }

    //Proceso de ocultar información.
    public byte[] embed(ProcessConfig config) {
        BmpImage carrierImage = bmpHandler.parseBmp(config.carrierData());

        // TODO: Instanciar el CryptoHandler opcional basado en la configuración.
        // Optional<CryptoHandler> cryptoHandler = CryptoFactory.create(config...);
        byte[] payloadToEmbed = payloadHandler.preparePayload(config.secretData(), config.inputFileName(), /* cryptoHandler */);

        SteganographyStrategy strategy = SteganographyFactory.getStrategy(config.stegAlgorithm());

        if (strategy.getCapacity(carrierImage.pixelData()) < payloadToEmbed.length) {
            throw new StegoException("El archivo portador no tiene suficiente capacidad para ocultar los datos.");
        }

        byte[] newPixelData = strategy.embed(carrierImage.pixelData(), payloadToEmbed);
        BmpImage outputImage = new BmpImage(carrierImage.header(), newPixelData);
        return bmpHandler.writeBmpToBytes(outputImage);
    }

    /**
     * Orquesta el proceso de extraer información.
     */
    public byte[] extract(ProcessConfig config) {
        // implementar camino inverso
    }

}