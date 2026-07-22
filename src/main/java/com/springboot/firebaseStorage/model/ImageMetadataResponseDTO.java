package com.springboot.firebaseStorage.model;

public record ImageMetadataResponseDTO(
        Long id,
        String storageFileName,
        String originalFileName,
        Long size,
        String mimeType) {
    public ImageMetadataResponseDTO(ImageMetadata imageMetadata) {
        this(
                imageMetadata.getId(),
                imageMetadata.getStorageFileName(),
                imageMetadata.getOriginalFileName(),
                imageMetadata.getSize(),
                imageMetadata.getMimeType()
        );
    }
}
