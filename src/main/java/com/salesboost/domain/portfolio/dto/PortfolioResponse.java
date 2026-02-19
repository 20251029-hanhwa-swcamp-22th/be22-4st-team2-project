package com.salesboost.domain.portfolio.dto;

import java.time.LocalDateTime;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PortfolioResponse {
    private Long id;
    private String title;
    private String description;
    private String clientName;
    private String industry;
    private String thumbnailUrl;
    private boolean visible;
    private int displayOrder;
    private List<String> imageUrls;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
