package com.salesboost.domain.portfolio.repository;

import com.salesboost.domain.portfolio.entity.Portfolio;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    List<Portfolio> findAllByOrderByDisplayOrderAscIdDesc();
    List<Portfolio> findAllByVisibleTrueOrderByDisplayOrderAscIdDesc();
}
