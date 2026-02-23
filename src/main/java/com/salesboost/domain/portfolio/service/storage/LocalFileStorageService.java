package com.salesboost.domain.portfolio.service.storage;

import com.salesboost.common.exception.BusinessException;
import com.salesboost.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "gif", "webp");
    private static final Set<String> ALLOWED_MIME_TYPES = Set.of(
            "image/jpeg", "image/png", "image/gif", "image/webp"
    );

    private final Path uploadRoot;

    public LocalFileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드 파일이 비어 있습니다.");
        }

        validateFileType(file);

        try {
            Files.createDirectories(uploadRoot);
            String originalName = sanitizeFilename(file.getOriginalFilename());
            String storedName = UUID.randomUUID() + "_" + originalName;
            Path target = uploadRoot.resolve(storedName).normalize();

            if (!target.startsWith(uploadRoot)) {
                throw new BusinessException(ErrorCode.INVALID_REQUEST, "잘못된 파일 경로입니다.");
            }

            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + storedName;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다.");
        }
    }

    private void validateFileType(MultipartFile file) {
        String originalName = file.getOriginalFilename();
        if (originalName == null || originalName.isBlank()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "파일명이 없습니다.");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_MIME_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "허용되지 않는 파일 형식입니다. (허용: jpg, png, gif, webp)");
        }

        String extension = getExtension(originalName).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST,
                    "허용되지 않는 파일 확장자입니다. (허용: jpg, png, gif, webp)");
        }
    }

    private String sanitizeFilename(String originalName) {
        if (originalName == null || originalName.isBlank()) {
            return "file";
        }
        return originalName.replaceAll("[^a-zA-Z0-9가-힣._-]", "_");
    }

    private String getExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex >= 0) ? filename.substring(dotIndex + 1) : "";
    }
}
