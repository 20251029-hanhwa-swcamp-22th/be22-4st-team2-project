package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.entity.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryDetailResponse {

    private Long id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private InquiryType inquiryType;
    private String content;
    private InquiryStatus status;
    private String adminMemo;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static InquiryDetailResponse from(Inquiry inquiry) {
        return InquiryDetailResponse.builder()
                .id(inquiry.getId())
                .companyName(inquiry.getCompanyName())
                .contactName(inquiry.getContactName())
                .email(inquiry.getEmail())
                .phone(inquiry.getPhone())
                .inquiryType(inquiry.getInquiryType())
                .content(inquiry.getContent())
                .status(inquiry.getStatus())
                .adminMemo(inquiry.getAdminMemo())
                .createdAt(inquiry.getCreatedAt())
                .updatedAt(inquiry.getUpdatedAt())
                .build();
    }
}
