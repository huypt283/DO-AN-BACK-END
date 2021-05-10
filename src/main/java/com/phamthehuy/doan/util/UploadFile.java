package com.phamthehuy.doan.util;

import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class UploadFile {
    private final Path root = Paths.get("src/main/resources/uploads");

    public void save(MultipartFile file) throws Exception {
        Files.copy(file.getInputStream(), this.root.resolve(Objects.requireNonNull(file.getOriginalFilename())));
    }

    public Stream<Path> loadAll() throws Exception {
        return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
    }

    public File load(String filename) {
        Path path = root.resolve(filename);
        File file = path.toFile();
        if (file.exists()) {
            return file;
        }
        return null;
    }

    public void deleteAll() {
        FileSystemUtils.deleteRecursively(root.toFile());
    }

    public void deleteOneFile(String filename) {
        Path path = root.resolve(filename);
        FileSystemUtils.deleteRecursively(path.toFile());
    }
}
