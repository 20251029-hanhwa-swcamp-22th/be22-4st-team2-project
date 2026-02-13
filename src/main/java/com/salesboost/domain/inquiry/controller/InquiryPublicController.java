package com.salesboost.domain.inquiry.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.inquiry.dto.InquiryCreateRequest;
import com.salesboost.domain.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inquiry Public API", description = "제휴 문의 공개 API")
@RestController
@RequestMapping("/api/inquiries")
@RequiredArgsConstructor
public class InquiryPublicController {

    private final InquiryService inquiryService;

    @Operation(summary = "제휴 문의 등록", description = "새로운 제휴 문의를 등록합니다.")
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createInquiry(@Valid @RequestBody InquiryCreateRequest request) {
        Long inquiryId = inquiryService.createInquiry(request);
        return ApiResponse.ok("제휴 문의가 정상적으로 등록되었습니다.", inquiryId);
    }
}
