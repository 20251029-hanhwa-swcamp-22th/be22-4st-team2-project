package com.salesboost.domain.portfolio.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PortfolioVisibilityOrderRequest {

    private Boolean isVisible;

    private Integer displayOrder;
}
