package com.salesboost.domain.portfolio.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.portfolio.dto.PortfolioCreateRequest;
import com.salesboost.domain.portfolio.dto.PortfolioResponse;
import com.salesboost.domain.portfolio.dto.PortfolioUpdateRequest;
import com.salesboost.domain.portfolio.dto.PortfolioVisibilityOrderRequest;
import com.salesboost.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Portfolio Admin API", description = "포트폴리오 관리 API")
@RestController
@RequestMapping("/api/admin/portfolios")
@RequiredArgsConstructor
public class PortfolioAdminController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 전체 목록 조회", description = "숨김 포함 전체 포트폴리오를 조회합니다.")
    @GetMapping
    public ApiResponse<List<PortfolioResponse>> getAllPortfolios() {
        List<PortfolioResponse> portfolios = portfolioService.getAllPortfolios();
        return ApiResponse.success(portfolios);
    }

    @Operation(summary = "포트폴리오 등록", description = "새로운 포트폴리오를 등록합니다. (이미지 업로드 포함)")
    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<Long> createPortfolio(@ModelAttribute PortfolioCreateRequest request) {
        Long portfolioId = portfolioService.createPortfolio(request);
        return ApiResponse.success("포트폴리오가 등록되었습니다.", portfolioId);
    }

    @Operation(summary = "포트폴리오 수정", description = "포트폴리오 정보를 수정합니다. (이미지 추가/삭제 포함)")
    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Void> updatePortfolio(
            @PathVariable Long id,
            @ModelAttribute PortfolioUpdateRequest request
    ) {
        portfolioService.updatePortfolio(id, request);
        return ApiResponse.success("포트폴리오가 수정되었습니다.", null);
    }

    @Operation(summary = "포트폴리오 삭제", description = "포트폴리오를 삭제합니다. (연관 이미지도 함께 삭제)")
    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ApiResponse.success("포트폴리오가 삭제되었습니다.", null);
    }

    @Operation(summary = "노출 여부 변경", description = "포트폴리오의 노출 여부를 변경합니다.")
    @PatchMapping("/{id}/visibility")
    public ApiResponse<Void> updateVisibility(
            @PathVariable Long id,
            @RequestBody PortfolioVisibilityOrderRequest request
    ) {
        if (request.getIsVisible() != null) {
            portfolioService.updateVisibility(id, request.getIsVisible());
        }
        return ApiResponse.success("노출 여부가 변경되었습니다.", null);
    }

    @Operation(summary = "정렬 순서 변경", description = "포트폴리오의 정렬 순서를 변경합니다.")
    @PatchMapping("/{id}/order")
    public ApiResponse<Void> updateDisplayOrder(
            @PathVariable Long id,
            @RequestBody PortfolioVisibilityOrderRequest request
    ) {
        if (request.getDisplayOrder() != null) {
            portfolioService.updateDisplayOrder(id, request.getDisplayOrder());
        }
        return ApiResponse.success("정렬 순서가 변경되었습니다.", null);
    }
}
