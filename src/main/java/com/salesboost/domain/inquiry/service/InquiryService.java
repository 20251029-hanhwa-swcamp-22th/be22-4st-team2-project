package com.salesboost.domain.inquiry.service;

import com.salesboost.domain.inquiry.dto.InquiryCreateRequest;
import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.repository.InquiryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    /**
     * 제휴 문의 등록 (Public API)
     */
    @Transactional
    public Long createInquiry(InquiryCreateRequest request) {
        Inquiry inquiry = request.toEntity();
        Inquiry savedInquiry = inquiryRepository.save(inquiry);

        log.info("제휴 문의 등록 완료 - ID: {}, 기업명: {}, 담당자: {}",
                savedInquiry.getId(), savedInquiry.getCompanyName(), savedInquiry.getContactName());

        return savedInquiry.getId();
    }

    /**
     * 문의 상태 변경 (Admin API)
     */
    @Transactional
    public void updateInquiryStatus(Long id, InquiryStatus status) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. ID: " + id));

        inquiry.updateStatus(status);
        log.info("문의 상태 변경 - ID: {}, 상태: {}", id, status);
    }

    /**
     * 관리자 메모 수정 (Admin API)
     */
    @Transactional
    public void updateInquiryMemo(Long id, String memo) {
        Inquiry inquiry = inquiryRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("문의를 찾을 수 없습니다. ID: " + id));

        inquiry.updateMemo(memo);
        log.info("문의 메모 수정 - ID: {}", id);
    }
}
