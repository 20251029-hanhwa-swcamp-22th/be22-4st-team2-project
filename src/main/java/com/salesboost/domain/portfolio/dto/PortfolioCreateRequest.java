package com.salesboost.domain.portfolio.dto;

import com.salesboost.domain.portfolio.entity.Portfolio;
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
public class PortfolioCreateRequest {

    @NotBlank(message = "제목은 필수입니다.")
    private String title;

    @NotBlank(message = "설명은 필수입니다.")
    private String description;

    @NotBlank(message = "고객사는 필수입니다.")
    private String client;

    @NotBlank(message = "업종은 필수입니다.")
    private String industry;

    private MultipartFile thumbnail;

    private List<MultipartFile> images;

    private Boolean isVisible;

    private Integer displayOrder;

    public Portfolio toEntity(String thumbnailUrl) {
        return Portfolio.builder()
                .title(title)
                .description(description)
                .client(client)
                .industry(industry)
                .thumbnailUrl(thumbnailUrl)
                .isVisible(isVisible != null ? isVisible : true)
                .displayOrder(displayOrder != null ? displayOrder : 0)
                .build();
    }
}
