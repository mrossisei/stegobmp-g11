package com.stegobmp.domain.steganography;

import java.io.ByteArrayOutputStream;

public class SteganographyStrategyLSB1 extends SteganographyStrategyAbs  {
    // recibe:
    // | size | datos | ext
    // | size | cifrado(size, datos, ext)


    public SteganographyStrategyLSB1() {
        super(StegAlgorithm.LSB1);
    }



}
