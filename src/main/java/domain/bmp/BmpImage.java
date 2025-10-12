package com.tudai.stegobmp.domain.bmp;

import java.util.Arrays;

/**
 * Un objeto inmutable (DTO) para representar un archivo BMP en memoria.
 * Separa el encabezado de los datos de los píxeles.
 *
 * @param header    Los 54 bytes del encabezado del archivo BMP.
 * @param pixelData Los bytes que componen la imagen (el cuerpo del archivo).
 */
public record BmpImage(byte[] header, byte[] pixelData) {



}