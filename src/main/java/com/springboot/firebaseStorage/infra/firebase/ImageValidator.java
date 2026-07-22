package com.springboot.firebaseStorage.infra.firebase;

import com.springboot.firebaseStorage.exceptions.FileSizeException;
import org.apache.tika.Tika;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Component
public class ImageValidator {
    //  Allowed file extensions
    private static final List<String> ALLOWED_MIME_TYPES = List.of("image/jpeg", "image/png", "image/webp", "image/jpg", "image/gif");
    private final Tika tika = new Tika();

    public void validateImage(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File cannot be empty");
        }

        if (file.getSize() > 5 * 1024 * 1024) {
            throw new FileSizeException("File size exceeds the maximum limit of 5MB");
        }

        try {
            // Verify Magic Bytes (Actual file content, not just extension) | prevent fake file extensions
            String detectedMimeType = tika.detect(file.getInputStream());

            if (!ALLOWED_MIME_TYPES.contains(detectedMimeType)) {
                throw new IllegalArgumentException("Invalid file type. Only JPEG, PNG, WEBP, JPG and GIF are allowed. Detected: " + detectedMimeType);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to validate file content", e);
        }
    }

    public String getSanitizedExtension(MultipartFile file) {
        try {
            String mimeType = tika.detect(file.getInputStream());
            return switch (mimeType) {
                case "image/jpeg", "image/jpg" -> ".jpg";
                case "image/png" -> ".png";
                case "image/webp" -> ".webp";
                case "image/gif" -> ".gif";
                default -> throw new IllegalArgumentException("Unsupported image format");
            };
        } catch (IOException e) {
            throw new RuntimeException("Failed to determine extension", e);
        }
    }
}
