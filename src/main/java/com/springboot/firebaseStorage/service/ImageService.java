package com.springboot.firebaseStorage.service;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import com.springboot.firebaseStorage.infra.firebase.ImageValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ImageService {
    private final ImageValidator imageValidator;
    @Value("${firebase.storage-bucket}")
    private final String bucketName;

    public String uploadImage(MultipartFile file) throws IOException {
        // Validate the image file
        imageValidator.validateImage(file);

        // Upload the image to Firebase Storage
        String extension = imageValidator.getSanitizedExtension(file);
        String fileName = UUID.randomUUID().toString() + "." + extension;
        String storagePath = "images/" + fileName;

        Bucket bucket = StorageClient.getInstance().bucket(bucketName);
        bucket.create(storagePath, file.getInputStream(), file.getContentType());

        return fileName;
    }

    public byte[] downloadImage(String fileName) throws IOException {
        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get("images/" + fileName);

        if (blob == null) {
            throw new FileNotFoundException("File not found: " + fileName);
        }

        return blob.getContent();
    }

    public String getMimeType(String fileName) {
        if (fileName.endsWith(".png")) return "image/png";
        if (fileName.endsWith(".webp")) return "image/webp";
        if (fileName.endsWith(".gif")) return "image/gif";
        return "image/jpeg";
    }
}
