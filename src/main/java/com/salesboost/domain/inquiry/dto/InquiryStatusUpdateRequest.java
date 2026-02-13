package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.InquiryStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryStatusUpdateRequest {

    @NotNull(message = "상태는 필수입니다.")
    private InquiryStatus status;
}
