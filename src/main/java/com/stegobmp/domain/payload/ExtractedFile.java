package com.stegobmp.domain.payload;


public record ExtractedFile(byte[] fileData, String extension) {}