package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.entity.InquiryType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class InquiryListItemResponse {

    private Long id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private InquiryType inquiryType;
    private InquiryStatus status;
    private LocalDateTime createdAt;

    public static InquiryListItemResponse from(Inquiry inquiry) {
        return InquiryListItemResponse.builder()
                .id(inquiry.getId())
                .companyName(inquiry.getCompanyName())
                .contactName(inquiry.getContactName())
                .email(inquiry.getEmail())
                .phone(inquiry.getPhone())
                .inquiryType(inquiry.getInquiryType())
                .status(inquiry.getStatus())
                .createdAt(inquiry.getCreatedAt())
                .build();
    }
}
