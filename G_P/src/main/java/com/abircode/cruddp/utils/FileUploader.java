package com.abircode.cruddp.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

public class FileUploader {
    private static final String UPLOAD_DIR = "src/main/resources/uploads/users/";

    public static String uploadImage(File imageFile) throws IOException {
        // Create upload directory if it doesn't exist
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // Generate unique filename
        String originalFileName = imageFile.getName();
        String fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        String uniqueFileName = UUID.randomUUID().toString() + fileExtension;

        // Copy file to upload directory
        Path targetLocation = uploadPath.resolve(uniqueFileName);
        Files.copy(imageFile.toPath(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

        return uniqueFileName;
    }
}