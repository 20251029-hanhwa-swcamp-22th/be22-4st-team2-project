package com.salesboost.domain.portfolio.repository;

import com.salesboost.domain.portfolio.entity.Portfolio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {
    @EntityGraph(attributePaths = {"images"})
    List<Portfolio> findAllByOrderByDisplayOrderAscIdDesc();

    @EntityGraph(attributePaths = {"images"})
    List<Portfolio> findAllByVisibleTrueOrderByDisplayOrderAscIdDesc();

    @EntityGraph(attributePaths = {"images"})
    Optional<Portfolio> findWithImagesById(Long id);
}
