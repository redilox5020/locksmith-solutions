package com.todoteg.cerrajeria.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class FileStorageService {

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir:uploads}") String dir) {
        this.uploadDir = Paths.get(dir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException e) {
            throw new RuntimeException("No se pudo crear el directorio de uploads", e);
        }
    }

    public String store(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf("."));
        }
        String filename = UUID.randomUUID() + ext;
        Path target = uploadDir.resolve(filename);
        try {
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new RuntimeException("Error almacenando archivo", e);
        }
        return filename;
    }

    /**
     * Elimina un archivo del directorio de uploads si existe.
     * Recibe la URL completa o solo el nombre del archivo.
     */
    public void deleteIfLocal(String url) {
        if (url == null || url.isBlank()) return;
        // Extraer el nombre del archivo de la URL (última parte después de /uploads/)
        String filename;
        int idx = url.indexOf("/uploads/");
        if (idx >= 0) {
            filename = url.substring(idx + "/uploads/".length());
        } else {
            // No es un archivo local subido, es una URL externa
            return;
        }
        try {
            Path file = uploadDir.resolve(filename).normalize();
            Files.deleteIfExists(file);
        } catch (IOException e) {
            // Log pero no interrumpir el flujo de eliminación
            System.err.println("No se pudo eliminar archivo: " + filename + " - " + e.getMessage());
        }
    }
}
