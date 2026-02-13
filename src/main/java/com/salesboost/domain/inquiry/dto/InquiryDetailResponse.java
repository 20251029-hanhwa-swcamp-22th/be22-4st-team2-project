package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.entity.InquiryType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
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
}
