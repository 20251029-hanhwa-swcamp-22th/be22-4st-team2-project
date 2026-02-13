package com.salesboost.domain.portfolio.repository;

import com.salesboost.domain.portfolio.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {
}
