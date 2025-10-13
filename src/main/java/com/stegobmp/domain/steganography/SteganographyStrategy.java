package com.stegobmp.domain.steganography;

/**
 * Interfaz para el patrón Strategy. Define el contrato que todos
 * los algoritmos de esteganografía deben cumplir.
 * Esto permite que la capa de servicio trabaje con cualquier algoritmo
 * de forma agnóstica.
 */
public interface SteganographyStrategy {

    /**
     * Oculta un payload dentro de los datos de píxeles de una imagen.
     * @param carrierPixelData El array de bytes del cuerpo de la imagen portadora.
     * @param payload El array de bytes de la información a ocultar.
     * @return Un nuevo array de bytes con la información oculta.
     */
    byte[] embed(byte[] carrierPixelData, byte[] payload);

    /**
     * Extrae un payload oculto de los datos de píxeles de una imagen.
     * @param carrierPixelData El array de bytes del cuerpo de la imagen portadora.
     * @return El array de bytes del payload oculto.
     */
    byte[] extract(byte[] carrierPixelData, boolean hasExtension);

    /**
     * Calcula la capacidad máxima de ocultamiento en bytes para un portador dado.
     * @param carrierPixelData El array de bytes del cuerpo de la imagen portadora.
     * @return La cantidad de bytes que se pueden ocultar.
     */
    int getCapacity(byte[] carrierPixelData);
}
