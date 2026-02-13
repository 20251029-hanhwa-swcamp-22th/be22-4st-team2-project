package com.salesboost.domain.inquiry.service;

import com.salesboost.common.exception.BusinessException;
import com.salesboost.common.exception.ErrorCode;
import com.salesboost.domain.inquiry.dto.InquiryCreateRequest;
import com.salesboost.domain.inquiry.dto.InquiryMemoUpdateRequest;
import com.salesboost.domain.inquiry.dto.InquiryStatusUpdateRequest;
import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public Long createInquiry(InquiryCreateRequest request) {
        Inquiry inquiry = request.toEntity();
        Inquiry saved = inquiryRepository.save(inquiry);
        return saved.getId();
    }

    public void updateStatus(Long inquiryId, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.changeStatus(request.getStatus());
    }

    public void updateMemo(Long inquiryId, InquiryMemoUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.INQUIRY_NOT_FOUND));
        inquiry.changeAdminMemo(request.getMemo());
    }
}
