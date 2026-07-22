package com.springboot.firebaseStorage.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.springboot.firebaseStorage.infra.firebase.ImageValidator;
import com.springboot.firebaseStorage.model.ImageMetadata;
import com.springboot.firebaseStorage.model.ImageMetadataResponseDTO;
import com.springboot.firebaseStorage.repository.ImageMetadataRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Service
public class ImageService {
    private final ImageValidator imageValidator;
    private final String bucketName;
    private final ImageMetadataRepository repository;

    public ImageService(ImageValidator imageValidator, @Value("${firebase.config.storage-bucket}") String bucketName, ImageMetadataRepository imageMetadataRepository) {
        this.imageValidator = imageValidator;
        this.bucketName = bucketName;
        this.repository = imageMetadataRepository;
    }

    public ImageMetadataResponseDTO uploadImage(MultipartFile file) throws IOException {
        // Validate the image file
        imageValidator.validateImage(file);

        // Upload the image to Firebase Storage
        String extension = imageValidator.getSanitizedExtension(file);
        String fileName = UUID.randomUUID().toString() + extension;
        String storagePath = "images/" + fileName;

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        bucket.create(storagePath, file.getInputStream(), file.getContentType());

        ImageMetadata imageMetadata = new ImageMetadata();

        imageMetadata.setSize(file.getSize());
        imageMetadata.setStorageFileName(fileName);
        imageMetadata.setOriginalFileName(file.getOriginalFilename());
        imageMetadata.setMimeType(file.getContentType());

        repository.save(imageMetadata);
        return new ImageMetadataResponseDTO(imageMetadata);
    }

    public byte[] downloadImage(String fileName) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get("images/" + fileName);

        if (blob == null) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        return blob.getContent();
    }

    public ImageMetadata getImageMetadataById(Long id) throws FileNotFoundException {
        return repository.findById(id)
                .orElseThrow(() -> new FileNotFoundException("File not found by id"));
    }

    public String getMimeType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".webp")) return "image/webp";
        if (fileName.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }
}
