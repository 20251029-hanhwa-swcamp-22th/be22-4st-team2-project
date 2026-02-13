package com.salesboost.domain.portfolio.service.storage;

import com.salesboost.common.exception.BusinessException;
import com.salesboost.common.exception.ErrorCode;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
public class LocalFileStorageService implements FileStorageService {

    private final Path uploadRoot;

    public LocalFileStorageService(@Value("${app.file.upload-dir:uploads}") String uploadDir) {
        this.uploadRoot = Path.of(uploadDir).toAbsolutePath().normalize();
    }

    @Override
    public String store(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "업로드 파일이 비어 있습니다.");
        }

        try {
            Files.createDirectories(uploadRoot);
            String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "_" + originalName;
            Path target = uploadRoot.resolve(storedName);
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
            return "/uploads/" + storedName;
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.INTERNAL_SERVER_ERROR, "파일 저장 중 오류가 발생했습니다.");
        }
    }
}
