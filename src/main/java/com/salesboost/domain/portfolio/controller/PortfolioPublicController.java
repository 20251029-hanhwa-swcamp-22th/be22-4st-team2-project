package com.salesboost.domain.portfolio.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.portfolio.dto.PortfolioResponse;
import com.salesboost.domain.portfolio.service.PortfolioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Portfolio Public API", description = "포트폴리오 공개 API")
@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioPublicController {

    private final PortfolioService portfolioService;

    @Operation(summary = "포트폴리오 목록 조회", description = "노출 중인 포트폴리오 목록을 조회합니다.")
    @GetMapping
    public ApiResponse<List<PortfolioResponse>> getPortfolios() {
        List<PortfolioResponse> portfolios = portfolioService.getVisiblePortfolios();
        return ApiResponse.success(portfolios);
    }

    @Operation(summary = "포트폴리오 상세 조회", description = "포트폴리오 상세 정보를 조회합니다.")
    @GetMapping("/{id}")
    public ApiResponse<PortfolioResponse> getPortfolio(@PathVariable Long id) {
        PortfolioResponse portfolio = portfolioService.getPortfolioById(id);
        return ApiResponse.success(portfolio);
    }
}
