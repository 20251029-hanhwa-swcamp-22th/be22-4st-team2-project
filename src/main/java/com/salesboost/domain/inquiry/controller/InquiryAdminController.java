package com.salesboost.domain.inquiry.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.inquiry.dto.InquiryDetailResponse;
import com.salesboost.domain.inquiry.dto.InquiryListResponse;
import com.salesboost.domain.inquiry.dto.InquiryMemoUpdateRequest;
import com.salesboost.domain.inquiry.dto.InquiryStatusUpdateRequest;
import com.salesboost.domain.inquiry.service.InquiryQueryService;
import com.salesboost.domain.inquiry.service.InquiryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryQueryService inquiryQueryService;
    private final InquiryService inquiryService;

    @GetMapping
    public ApiResponse<InquiryListResponse> getInquiries(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "latest") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ApiResponse.ok(inquiryQueryService.getInquiries(status, keyword, sort, page, size));
    }

    @GetMapping("/{id}")
    public ApiResponse<InquiryDetailResponse> getInquiry(@PathVariable Long id) {
        return ApiResponse.ok(inquiryQueryService.getInquiry(id));
    }

    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateStatus(@PathVariable Long id, @Valid @RequestBody InquiryStatusUpdateRequest request) {
        inquiryService.updateStatus(id, request);
        return ApiResponse.ok("문의 상태가 변경되었습니다.", null);
    }

    @PatchMapping("/{id}/memo")
    public ApiResponse<Void> updateMemo(@PathVariable Long id, @Valid @RequestBody InquiryMemoUpdateRequest request) {
        inquiryService.updateMemo(id, request);
        return ApiResponse.ok("관리자 메모가 변경되었습니다.", null);
    }
}
