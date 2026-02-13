package com.salesboost.domain.inquiry.dto;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InquiryMemoUpdateRequest {

    @Size(max = 3000, message = "메모는 3000자 이하여야 합니다.")
    private String memo;
}
