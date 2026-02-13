package com.salesboost.domain.portfolio.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    /**
     * 파일 저장
     * @param file 업로드할 파일
     * @return 저장된 파일의 URL 또는 경로
     */
    String store(MultipartFile file);

    /**
     * 파일 삭제
     * @param fileUrl 삭제할 파일의 URL 또는 경로
     */
    void delete(String fileUrl);
}
