package com.salesboost.domain.portfolio.repository;

import com.salesboost.domain.portfolio.entity.PortfolioImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface PortfolioImageRepository extends JpaRepository<PortfolioImage, Long> {

    List<PortfolioImage> findByPortfolioIdOrderByDisplayOrderAsc(Long portfolioId);

    void deleteByPortfolioId(Long portfolioId);
}
