package com.springboot.firebaseStorage.repository;

import com.springboot.firebaseStorage.model.ImageMetadata;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageMetadataRepository extends JpaRepository<ImageMetadata,Long> {
    ImageMetadata findByStorageFileName(String storageFileName);
}
