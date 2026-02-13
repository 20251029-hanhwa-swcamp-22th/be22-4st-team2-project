package com.salesboost.domain.portfolio.service.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadPath;
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif");
    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB

    public LocalFileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadPath);
            log.info("파일 업로드 디렉토리 생성: {}", this.uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("파일 업로드 디렉토리를 생성할 수 없습니다.", e);
        }
    }

    @Override
    public String store(MultipartFile file) {
        validateFile(file);

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        String fileExtension = getFileExtension(originalFilename);
        String storedFilename = UUID.randomUUID().toString() + "." + fileExtension;

        try {
            Path targetLocation = this.uploadPath.resolve(storedFilename);
            Files.copy(file.getInputStream(), targetLocation, StandardCopyOption.REPLACE_EXISTING);

            log.info("파일 저장 완료: {}", storedFilename);
            return "/uploads/" + storedFilename;
        } catch (IOException e) {
            log.error("파일 저장 실패: {}", originalFilename, e);
            throw new RuntimeException("파일 저장에 실패했습니다.", e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isEmpty()) {
            return;
        }

        try {
            String filename = fileUrl.substring(fileUrl.lastIndexOf("/") + 1);
            Path filePath = this.uploadPath.resolve(filename);
            Files.deleteIfExists(filePath);
            log.info("파일 삭제 완료: {}", filename);
        } catch (IOException e) {
            log.error("파일 삭제 실패: {}", fileUrl, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("빈 파일은 업로드할 수 없습니다.");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("파일 크기는 10MB를 초과할 수 없습니다.");
        }

        String originalFilename = file.getOriginalFilename();
        String fileExtension = getFileExtension(originalFilename);

        if (!ALLOWED_EXTENSIONS.contains(fileExtension.toLowerCase())) {
            throw new IllegalArgumentException("허용되지 않는 파일 형식입니다. (jpg, jpeg, png, gif만 가능)");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            throw new IllegalArgumentException("잘못된 파일명입니다.");
        }
        return filename.substring(filename.lastIndexOf(".") + 1);
    }
}
