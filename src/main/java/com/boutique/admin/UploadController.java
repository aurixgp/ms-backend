package com.boutique.admin;

import com.boutique.common.AppProperties;
import com.boutique.common.Exceptions;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin/uploads")
public class UploadController {

    private static final Map<String, String> EXTENSIONS = Map.of(
            "image/jpeg", ".jpg", "image/png", ".png", "image/webp", ".webp", "image/avif", ".avif");

    public record Uploaded(String url) {}

    private final Path dir;

    public UploadController(AppProperties props) throws IOException {
        this.dir = Path.of(props.uploadDir()).toAbsolutePath();
        Files.createDirectories(dir);
    }

    @PostMapping(consumes = "multipart/form-data")
    @ResponseStatus(HttpStatus.CREATED)
    public Uploaded upload(@RequestParam("file") MultipartFile file) throws IOException {
        String ext = EXTENSIONS.get(file.getContentType());
        if (ext == null) throw new Exceptions.BadRequest("Formats acceptés : JPEG, PNG, WebP, AVIF");
        String name = UUID.randomUUID() + ext;
        try (InputStream in = file.getInputStream()) {
            Files.copy(in, dir.resolve(name), StandardCopyOption.REPLACE_EXISTING);
        }
        return new Uploaded("/uploads/" + name);
    }
}
