package com.salesboost.domain.portfolio.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "portfolio_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PortfolioImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "portfolio_id", nullable = false)
    private Portfolio portfolio;

    @Column(nullable = false, length = 1000)
    private String imageUrl;

    @Column(nullable = false)
    private int imageOrder;

    public static PortfolioImage create(Portfolio portfolio, String imageUrl, int imageOrder) {
        PortfolioImage image = new PortfolioImage();
        image.portfolio = portfolio;
        image.imageUrl = imageUrl;
        image.imageOrder = imageOrder;
        return image;
    }
}
