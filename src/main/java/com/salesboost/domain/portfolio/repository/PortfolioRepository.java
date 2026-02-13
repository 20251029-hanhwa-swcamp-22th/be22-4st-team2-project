package com.salesboost.domain.portfolio.repository;

import com.salesboost.domain.portfolio.entity.Portfolio;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PortfolioRepository extends JpaRepository<Portfolio, Long> {

    /**
     * 노출 중인 포트폴리오 목록 조회 (Public API용)
     */
    List<Portfolio> findByIsVisibleTrueOrderByDisplayOrderAsc();

    /**
     * 전체 포트폴리오 목록 조회 (Admin API용)
     */
    List<Portfolio> findAllByOrderByDisplayOrderAsc();

    /**
     * 포트폴리오 상세 조회 (이미지 포함)
     */
    @Query("SELECT p FROM Portfolio p LEFT JOIN FETCH p.images WHERE p.id = :id")
    Optional<Portfolio> findByIdWithImages(Long id);
}
