package com.salesboost.domain.inquiry.dto;

import com.salesboost.domain.inquiry.entity.Inquiry;
import com.salesboost.domain.inquiry.entity.InquiryType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class InquiryCreateRequest {

    @NotBlank(message = "기업명은 필수입니다.")
    private String companyName;

    @NotBlank(message = "담당자명은 필수입니다.")
    private String contactName;

    @NotBlank(message = "이메일은 필수입니다.")
    @Email(message = "올바른 이메일 형식이 아닙니다.")
    private String email;

    @NotBlank(message = "연락처는 필수입니다.")
    @Pattern(regexp = "^\\d{2,3}-\\d{3,4}-\\d{4}$", message = "올바른 전화번호 형식이 아닙니다. (예: 010-1234-5678)")
    private String phone;

    @NotNull(message = "문의 유형은 필수입니다.")
    private InquiryType inquiryType;

    @NotBlank(message = "문의 내용은 필수입니다.")
    private String content;

    public Inquiry toEntity() {
        return Inquiry.builder()
                .companyName(companyName)
                .contactName(contactName)
                .email(email)
                .phone(phone)
                .inquiryType(inquiryType)
                .content(content)
                .build();
    }
}
