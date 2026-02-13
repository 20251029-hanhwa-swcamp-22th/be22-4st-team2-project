package com.salesboost.domain.inquiry.repository;

import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    /**
     * 전체 문의 목록 페이징 조회
     */
    Page<Inquiry> findAll(Pageable pageable);

    /**
     * 상태별 문의 목록 페이징 조회
     */
    Page<Inquiry> findByStatus(InquiryStatus status, Pageable pageable);

    /**
     * 상태별 문의 개수 조회
     */
    long countByStatus(InquiryStatus status);
}
