package com.stegobmp.service;

import com.stegobmp.domain.bmp.BmpHandler;
import com.stegobmp.domain.bmp.BmpImage;
import com.stegobmp.domain.payload.PayloadHandler;
import com.stegobmp.domain.steganography.SteganographyFactory;
import com.stegobmp.domain.steganography.SteganographyStrategy;
import com.stegobmp.exception.StegoException;

import java.io.IOException;
import java.util.Optional;

/**
 * Orquestador principal de la aplicación.
 * Coordina las llamadas a los componentes del dominio (handlers, strategies, etc.).
 */
public class StegoBmpService {

    private final BmpHandler bmpHandler;
    private final PayloadHandler payloadHandler;
    private final ServiceConfig config;

    public StegoBmpService(ServiceConfig serviceConfig) {
        this.bmpHandler = new BmpHandler();
        this.payloadHandler = new PayloadHandler();
        this.config = serviceConfig;
    }

    //Proceso de ocultar información.
    public byte[] embed() throws IOException {
        BmpImage carrierImage = bmpHandler.parseBmp(config.carrierData());

        // TODO: Instanciar el CryptoHandler opcional basado en la configuración.
        // Optional<CryptoHandler> cryptoHandler = CryptoFactory.create(config...);
        byte[] payloadToEmbed = payloadHandler.preparePayload(config.secretData(), config.inputFileName(), Optional.empty());

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
    public byte[] extract() {
        // implementar camino inverso
        return null;
    }


}