package com.javarush.jira.bugtracking.attachment;

import com.javarush.jira.common.error.IllegalRequestDataException;
import com.javarush.jira.common.error.NotFoundException;
import lombok.experimental.UtilityClass;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@UtilityClass
public class FileUtil {
    private static final Logger logger = LoggerFactory.getLogger(FileUtil.class);
    private static final String ATTACHMENT_PATH = "./attachments/%s/";

    public static void upload(MultipartFile multipartFile, String directoryPath, String fileName) {
        if (multipartFile.isEmpty()) {
            throw new IllegalRequestDataException("Select a file to upload.");
        }

        Path directoriPath = Paths.get(directoryPath);
        try {
            if (!Files.exists(directoriPath)) {
                Files.createDirectories(directoriPath);
            }

            Path filePath = directoriPath.resolve(fileName);
            Files.copy(multipartFile.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            logger.info("File uploaded successfully: {}", filePath);
        } catch (IOException ex) {
            logger.error("Failed to upload file: {}", multipartFile.getOriginalFilename(), ex);
            throw new IllegalRequestDataException("Failed to upload file: " + multipartFile.getOriginalFilename());
        }
    }

    public static Resource download(String fileLink) {
        Path path = Paths.get(fileLink);
        try {
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                logger.info("File downloaded successfully: {}", fileLink);
                return resource;
            } else {
                logger.error("Failed to download file: {}", fileLink);
                throw new IllegalRequestDataException("Failed to download file: " + resource.getFilename());
            }
        } catch (MalformedURLException ex) {
            logger.error("File not found: {}", fileLink, ex);
            throw new NotFoundException("File not found: " + fileLink);
        }
    }

    public static void delete(String fileLink) {
        Path path = Paths.get(fileLink);
        try {
            Files.delete(path);
            logger.info("File deleted successfully: {}", fileLink);
        } catch (IOException ex) {
            logger.error("File deletion failed: {}", fileLink, ex);
            throw new IllegalRequestDataException("File deletion failed: " + fileLink);
        }
    }

    public static String getPath(String titleType) {
        return String.format(ATTACHMENT_PATH, titleType.toLowerCase());
    }
}
