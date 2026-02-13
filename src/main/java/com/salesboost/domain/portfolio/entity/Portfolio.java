package com.salesboost.domain.portfolio.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "portfolio")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Portfolio {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, length = 3000)
    private String description;

    @Column(nullable = false, length = 150)
    private String clientName;

    @Column(nullable = false, length = 100)
    private String industry;

    @Column(length = 1000)
    private String thumbnailUrl;

    @Column(nullable = false)
    private boolean visible;

    @Column(nullable = false)
    private int displayOrder;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "portfolio", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<PortfolioImage> images = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public static Portfolio create(String title, String description, String clientName, String industry, String thumbnailUrl) {
        Portfolio portfolio = new Portfolio();
        portfolio.title = title;
        portfolio.description = description;
        portfolio.clientName = clientName;
        portfolio.industry = industry;
        portfolio.thumbnailUrl = thumbnailUrl;
        portfolio.visible = true;
        portfolio.displayOrder = 0;
        return portfolio;
    }

    public void update(String title, String description, String clientName, String industry, String thumbnailUrl) {
        this.title = title;
        this.description = description;
        this.clientName = clientName;
        this.industry = industry;
        this.thumbnailUrl = thumbnailUrl;
    }

    public void updateVisibility(boolean visible) {
        this.visible = visible;
    }

    public void updateDisplayOrder(int displayOrder) {
        this.displayOrder = displayOrder;
    }

    public void clearImages() {
        this.images.clear();
    }

    public void addImage(PortfolioImage image) {
        this.images.add(image);
    }
}
