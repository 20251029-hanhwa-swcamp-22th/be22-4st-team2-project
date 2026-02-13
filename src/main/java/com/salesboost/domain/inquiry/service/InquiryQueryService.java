package com.salesboost.domain.inquiry.service;

import com.salesboost.domain.inquiry.dto.InquiryDetailResponse;
import com.salesboost.domain.inquiry.dto.InquiryListItemResponse;
import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.mapper.InquiryQueryMapper;
import com.salesboost.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService {

    private final InquiryQueryMapper inquiryQueryMapper;
    private final InquiryRepository inquiryRepository;

    /**
     * 문의 목록 조회 (필터링, 검색, 페이징)
     */
    public Page<InquiryListItemResponse> getInquiries(
            InquiryStatus status,
            String keyword,
            Pageable pageable
    ) {
        int offset = (int) pageable.getOffset();
        int size = pageable.getPageSize();

        List<InquiryListItemResponse> inquiries = inquiryQueryMapper.findInquiries(
                status, keyword, offset, size
        );

        long total = inquiryQueryMapper.countInquiries(status, keyword);

        return new PageImpl<>(inquiries, pageable, total);
    }

    /**
     * 문의 상세 조회
     */
    public InquiryDetailResponse getInquiryDetail(Long id) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. ID: " + id));

        return InquiryDetailResponse.from(inquiry);
    }
}
