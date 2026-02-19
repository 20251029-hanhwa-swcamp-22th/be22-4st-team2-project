package com.salesboost.domain.portfolio.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.portfolio.dto.*;
import com.salesboost.domain.portfolio.service.PortfolioService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/admin/portfolios")
@RequiredArgsConstructor
public class PortfolioAdminController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ApiResponse<List<PortfolioResponse>> getPortfolios() {
        return ApiResponse.ok(portfolioService.getAdminPortfolios());
    }

    @PostMapping
    public ApiResponse<Long> createPortfolio(@Valid @RequestBody PortfolioCreateRequest request) {
        return ApiResponse.ok("포트폴리오가 등록되었습니다.", portfolioService.createPortfolio(request));
    }

    @PutMapping("/{id}")
    public ApiResponse<Void> updatePortfolio(
            @PathVariable Long id,
            @Valid @RequestBody PortfolioUpdateRequest request
    ) {
        portfolioService.updatePortfolio(id, request);
        return ApiResponse.ok("포트폴리오가 수정되었습니다.", null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> deletePortfolio(@PathVariable Long id) {
        portfolioService.deletePortfolio(id);
        return ApiResponse.ok("포트폴리오가 삭제되었습니다.", null);
    }

    @PatchMapping("/{id}/visibility")
    public ApiResponse<Void> updateVisibility(@PathVariable Long id, @Valid @RequestBody PortfolioVisibilityRequest request) {
        portfolioService.updateVisibility(id, request);
        return ApiResponse.ok("노출 여부가 변경되었습니다.", null);
    }

    @PatchMapping("/order")
    public ApiResponse<Void> updateOrder(@Valid @RequestBody PortfolioVisibilityOrderRequest request) {
        portfolioService.updateOrder(request);
        return ApiResponse.ok("노출 순서가 변경되었습니다.", null);
    }
}
