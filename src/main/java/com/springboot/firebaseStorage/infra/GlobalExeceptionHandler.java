package com.springboot.firebaseStorage.infra;

import com.springboot.firebaseStorage.exceptions.FileSizeException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExeceptionHandler {
    private ResponseEntity<Map<String, Object>> buildError(HttpStatus status, String error, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", System.currentTimeMillis());
        body.put("status", status);
        body.put("error", error);
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }

    @ExceptionHandler(Exception.class)
    private ResponseEntity<Map<String,Object>> handleGenericException(Exception ex) {
        return this.buildError(HttpStatus.BAD_REQUEST, "Error during process", ex.getMessage());
    }

    @ExceptionHandler(FileNotFoundException.class)
    private ResponseEntity<Map<String,Object>> handleFileNotFoundException(FileNotFoundException ex) {
        return this.buildError(HttpStatus.NOT_FOUND, "File not found", ex.getMessage());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<Map<String,Object>> handleIllegalArgumentException(IllegalArgumentException ex) {
        return this.buildError(HttpStatus.BAD_REQUEST, "Illegal Argument", ex.getMessage());
    }

    @ExceptionHandler(IOException.class)
    private ResponseEntity<Map<String,Object>> handleIOException(IOException ex) {
        return this.buildError(HttpStatus.INTERNAL_SERVER_ERROR, "File IO Error", ex.getMessage());
    }

    @ExceptionHandler(FileSizeException.class)
    private ResponseEntity<Map<String,Object>> handleFileSizeException(FileSizeException ex) {
        return this.buildError(HttpStatus.BAD_REQUEST, "File Size Error", ex.getMessage());
    }

}
