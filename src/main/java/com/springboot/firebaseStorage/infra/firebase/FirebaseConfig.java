package com.springboot.firebaseStorage.infra.firebase;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import jakarta.annotation.PostConstruct;
import java.io.IOException;

@Configuration
public class FirebaseConfig {
    @Value("${firebase.config.service-account-path}")
    private Resource serviceAccountFile;

    @Value("${firebase.config.storage-bucket}")
    private String storageBucket;

    @PostConstruct
    public void initializeFirebase() {
        try {
            FirebaseOptions options = FirebaseOptions.builder()
                    .setCredentials(GoogleCredentials.fromStream(serviceAccountFile.getInputStream()))
                    .setStorageBucket(storageBucket)
                    .build();

            if (FirebaseApp.getApps().isEmpty()) {
                FirebaseApp.initializeApp(options);
                System.out.println("Firebase application has been initialized successfully.");
            }
        } catch (IOException e) {
            throw new IllegalStateException("Failed to initialize Firebase application", e);
        }
    }
}
