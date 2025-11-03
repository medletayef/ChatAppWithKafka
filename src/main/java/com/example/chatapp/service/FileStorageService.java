package com.example.chatapp.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private final Path uploadDir = Paths.get("uploads/avatars").toAbsolutePath().normalize();

    public FileStorageService() throws IOException {
        Files.createDirectories(uploadDir);
    }

    public String storeFile(MultipartFile file) {
        String fileName = UUID.randomUUID() + "_" + StringUtils.cleanPath(file.getOriginalFilename());
        try {
            Path target = uploadDir.resolve(fileName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/avatars/" + fileName; // This is the URL you’ll save in DB
        } catch (IOException e) {
            throw new RuntimeException("Could not store file: " + fileName, e);
        }
    }
}
