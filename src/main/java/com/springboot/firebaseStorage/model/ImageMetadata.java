package com.springboot.firebaseStorage.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "images")
@NoArgsConstructor
@Setter
@Getter
public class ImageMetadata {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // UUID From Firebase Storage
    private String storageFileName;

    // User's original upload FileName
    private String originalFileName;

    private Long Size;
    private String mimeType;
}
