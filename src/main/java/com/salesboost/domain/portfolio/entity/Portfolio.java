package com.salesboost.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "portfolios")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, length = 100)
    private String client;

    @Column(nullable = false, length = 100)
    private String industry;

    @Column(length = 500)
    private String thumbnailUrl;

    @Column(nullable = false)
    private Boolean isVisible = true;

    @Column(nullable = false)
    private Integer displayOrder = 0;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioImage> images = new ArrayList<>();

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Portfolio(String title, String description, String client, String industry, String thumbnailUrl, Boolean isVisible, Integer displayOrder) {
        this.title = title;
        this.description = description;
        this.client = client;
        this.industry = industry;
        this.thumbnailUrl = thumbnailUrl;
        this.isVisible = isVisible != null ? isVisible : true;
        this.displayOrder = displayOrder != null ? displayOrder : 0;
    }

    public void update(String title, String description, String client, String industry, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.client = client;
        this.industry = industry;
        if (thumbnailUrl != null) {
            this.thumbnailUrl = thumbnailUrl;
        }
    }

    public void updateVisibility(Boolean isVisible) {
        this.isVisible = isVisible;
    }

    public void updateDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void addImage(PortfolioImage image) {
        this.images.add(image);
        image.setPortfolio(this);
    }

    public void removeImage(PortfolioImage image) {
        this.images.remove(image);
        image.setPortfolio(null);
    }
}
