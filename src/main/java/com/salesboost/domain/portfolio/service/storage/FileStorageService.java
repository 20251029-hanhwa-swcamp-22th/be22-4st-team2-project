package com.salesboost.domain.portfolio.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {
    String store(MultipartFile file);
}
