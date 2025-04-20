package org.example.dcdemo.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Service
public class FileService {
    
    @Value("${file.upload-dir}")
    private String uploadDir;
    
    @Value("${file.access-path:/uploads/}")
    private String accessPath;
    
    @Value("${file.domain:}")
    private String domain;
    
    public String saveImage(MultipartFile file) throws IOException {
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        String filename = UUID.randomUUID() + getFileExtension(file.getOriginalFilename());
        Path filePath = uploadPath.resolve(filename);
        
        Files.deleteIfExists(filePath);
        Files.copy(file.getInputStream(), filePath);

        // 返回完整的访问URL
        return domain + accessPath + filename;
    }
    
    private String getFileExtension(String filename) {
        return filename.substring(filename.lastIndexOf("."));
    }
} 