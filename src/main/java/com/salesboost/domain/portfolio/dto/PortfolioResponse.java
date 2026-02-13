package com.salesboost.domain.portfolio.dto;

import com.salesboost.domain.portfolio.entity.Portfolio;
import com.salesboost.domain.portfolio.entity.PortfolioImage;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class PortfolioResponse {

    private Long id;
    private String title;
    private String description;
    private String client;
    private String industry;
    private String thumbnailUrl;
    private Boolean isVisible;
    private Integer displayOrder;
    private List<PortfolioImageDto> images;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static PortfolioResponse from(Portfolio portfolio) {
        return PortfolioResponse.builder()
                .id(portfolio.getId())
                .title(portfolio.getTitle())
                .description(portfolio.getDescription())
                .client(portfolio.getClient())
                .industry(portfolio.getIndustry())
                .thumbnailUrl(portfolio.getThumbnailUrl())
                .isVisible(portfolio.getIsVisible())
                .displayOrder(portfolio.getDisplayOrder())
                .images(portfolio.getImages().stream()
                        .map(PortfolioImageDto::from)
                        .collect(Collectors.toList()))
                .createdAt(portfolio.getCreatedAt())
                .updatedAt(portfolio.getUpdatedAt())
                .build();
    }

    @Getter
    @Builder
    public static class PortfolioImageDto {
        private Long id;
        private String imageUrl;
        private Integer displayOrder;

        public static PortfolioImageDto from(PortfolioImage image) {
            return PortfolioImageDto.builder()
                    .id(image.getId())
                    .imageUrl(image.getImageUrl())
                    .displayOrder(image.getDisplayOrder())
                    .build();
        }
    }
}
