package com.phamthehuy.doan.util;

import com.phamthehuy.doan.exception.BadRequestException;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;
import java.util.stream.Stream;

@Service
public class UploadFile {
    private final Path root = Paths.get("src/main/resources/uploads");
    @EventListener
    public void init(ApplicationReadyEvent event) throws BadRequestException {
        try {
            if(!root.toFile().exists()) Files.createDirectory(root);
        } catch (IOException e) {
            throw new BadRequestException("Could not initialize folder for upload!");
        }
    }

    //upload
    public void save(MultipartFile file) throws BadRequestException {
        try {
            Files.copy(file.getInputStream(), this.root.resolve(Objects.requireNonNull(file.getOriginalFilename())));
        } catch (Exception e) {
            throw new BadRequestException("Could not store the file. Error: " + e.getMessage());
        }
    }
    // all files
    public Stream<Path> loadAll() throws BadRequestException {
        try {
            return Files.walk(this.root, 1).filter(path -> !path.equals(this.root)).map(this.root::relativize);
        } catch (IOException e) {
            throw new BadRequestException("Could not load the files!");
        }
    }
    // one file
    public File load(String filename) throws BadRequestException {
        try {
            Path path = root.resolve(filename);
            File file= path.toFile();
            if(file.exists()){
                return file;
            }else{
                throw new BadRequestException("Could not read the file!");
            }
        } catch (Exception e) {
            throw new BadRequestException("Error: " + e.getMessage());
        }
    }
    //delete all file
    public void deleteAll() throws BadRequestException {
        FileSystemUtils.deleteRecursively(root.toFile());
    }
    // delete one file
    public void deleteOneFile(String filename) throws BadRequestException {
        Path path = root.resolve(filename);
        FileSystemUtils.deleteRecursively(path.toFile());
    }
}
