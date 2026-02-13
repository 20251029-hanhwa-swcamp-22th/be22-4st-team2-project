package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryStatusUpdateRequest {

    @NotNull(message = "status는 필수입니다.")
    private InquiryStatus status;
}
