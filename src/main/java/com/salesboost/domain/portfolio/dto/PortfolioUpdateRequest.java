package com.salesboost.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioUpdateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    @NotBlank(message = "고객사는 필수입니다.")
    private String client;

    @NotBlank(message = "업종은 필수입니다.")
    private String industry;

    private MultipartFile thumbnail;

    private List<MultipartFile> newImages;

    private List<Long> deleteImageIds;
}
