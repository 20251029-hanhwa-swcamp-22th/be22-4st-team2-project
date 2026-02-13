package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.entity.InquiryType;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryListItemResponse {
    private Long id;
    private String companyName;
    private String contactName;
    private String email;
    private String phone;
    private InquiryType inquiryType;
    private InquiryStatus status;
    private LocalDateTime createdAt;
}
