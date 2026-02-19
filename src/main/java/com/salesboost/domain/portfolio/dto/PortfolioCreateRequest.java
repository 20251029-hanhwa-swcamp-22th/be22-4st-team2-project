package com.salesboost.domain.portfolio.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortfolioCreateRequest {

    @NotBlank(message = "title은 필수입니다.")
    private String title;

    @NotBlank(message = "description은 필수입니다.")
    private String description;

    @NotBlank(message = "clientName은 필수입니다.")
    private String clientName;

    @NotBlank(message = "industry는 필수입니다.")
    private String industry;

    private String thumbnailUrl;

    private boolean visible = true;
}
