package com.salesboost.domain.portfolio.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class PortfolioVisibilityRequest {

    @NotNull(message = "visible은 필수입니다.")
    private Boolean visible;
}
