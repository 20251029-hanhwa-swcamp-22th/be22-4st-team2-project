package com.salesboost.domain.inquiry.service;

import com.salesboost.common.exception.BusinessException;
import com.salesboost.common.exception.ErrorCode;
import com.salesboost.domain.inquiry.dto.InquiryDetailResponse;
import com.salesboost.domain.inquiry.dto.InquiryListItemResponse;
import com.salesboost.domain.inquiry.dto.InquiryListResponse;
import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.mapper.InquiryQueryMapper;
import com.salesboost.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryQueryService {

    private final InquiryQueryMapper inquiryQueryMapper;
    private final InquiryRepository inquiryRepository;

    public InquiryListResponse getInquiries(String status, String keyword, String sort, int page, int size) {
        int safePage = Math.max(page, 1);
        int safeSize = Math.max(size, 1);
        int offset = (safePage - 1) * safeSize;

        String safeSort = (sort == null || sort.isBlank()) ? "latest" : sort;
        if (!safeSort.equals("latest") && !safeSort.equals("oldest")) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "sort는 latest 또는 oldest만 가능합니다.");
        }

        var items = inquiryQueryMapper.findInquiries(status, keyword, safeSort, safeSize, offset);
        long totalCount = inquiryQueryMapper.countInquiries(status, keyword);

        return new InquiryListResponse(items, totalCount, safePage, safeSize);
    }

    public InquiryDetailResponse getInquiry(Long inquiryId) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));

        return new InquiryDetailResponse(
                inquiry.getId(),
                inquiry.getCompanyName(),
                inquiry.getContactName(),
                inquiry.getEmail(),
                inquiry.getPhone(),
                inquiry.getInquiryType(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getAdminMemo(),
                inquiry.getCreatedAt(),
                inquiry.getUpdatedAt()
        );
    }
}
