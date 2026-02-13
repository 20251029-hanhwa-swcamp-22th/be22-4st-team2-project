package com.salesboost.domain.inquiry.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.inquiry.dto.InquiryDetailResponse;
import com.salesboost.domain.inquiry.dto.InquiryListItemResponse;
import com.salesboost.domain.inquiry.dto.InquiryMemoUpdateRequest;
import com.salesboost.domain.inquiry.dto.InquiryStatusUpdateRequest;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import com.salesboost.domain.inquiry.service.InquiryQueryService;
import com.salesboost.domain.inquiry.service.InquiryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Inquiry Admin API", description = "제휴 문의 관리 API")
@RestController
@RequestMapping("/api/admin/inquiries")
@RequiredArgsConstructor
public class InquiryAdminController {

    private final InquiryQueryService inquiryQueryService;
    private final InquiryService inquiryService;

    @Operation(summary = "문의 목록 조회", description = "필터링, 검색, 페이징 기능을 제공하는 문의 목록 조회")
    @GetMapping
    public ApiResponse<Page<InquiryListItemResponse>> getInquiries(
            @RequestParam(required = false) InquiryStatus status,
            @RequestParam(required = false) String keyword,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        Page<InquiryListItemResponse> inquiries = inquiryQueryService.getInquiries(status, keyword, pageable);
        return ApiResponse.success(inquiries);
    }

    @Operation(summary = "문의 상세 조회", description = "문의 ID로 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<InquiryDetailResponse> getInquiry(@PathVariable Long id) {
        InquiryDetailResponse inquiry = inquiryQueryService.getInquiryDetail(id);
        return ApiResponse.success(inquiry);
    }

    @Operation(summary = "문의 상태 변경", description = "문의 처리 상태를 변경합니다. (PENDING, IN_PROGRESS, DONE)")
    @PatchMapping("/{id}/status")
    public ApiResponse<Void> updateInquiryStatus(
            @PathVariable Long id,
            @Valid @RequestBody InquiryStatusUpdateRequest request
    ) {
        inquiryService.updateInquiryStatus(id, request.getStatus());
        return ApiResponse.success("문의 상태가 변경되었습니다.", null);
    }

    @Operation(summary = "관리자 메모 수정", description = "문의에 대한 관리자 메모를 수정합니다.")
    @PatchMapping("/{id}/memo")
    public ApiResponse<Void> updateInquiryMemo(
            @PathVariable Long id,
            @Valid @RequestBody InquiryMemoUpdateRequest request
    ) {
        inquiryService.updateInquiryMemo(id, request.getMemo());
        return ApiResponse.success("관리자 메모가 수정되었습니다.", null);
    }
}
