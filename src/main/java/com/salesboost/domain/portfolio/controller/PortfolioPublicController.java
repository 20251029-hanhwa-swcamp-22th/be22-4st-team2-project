package com.salesboost.domain.portfolio.controller;

import com.salesboost.common.response.ApiResponse;
import com.salesboost.domain.portfolio.dto.PortfolioResponse;
import com.salesboost.domain.portfolio.service.PortfolioService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/portfolios")
@RequiredArgsConstructor
public class PortfolioPublicController {

    private final PortfolioService portfolioService;

    @GetMapping
    public ApiResponse<List<PortfolioResponse>> getPortfolios() {
        return ApiResponse.ok(portfolioService.getPublicPortfolios());
    }

    @GetMapping("/{id}")
    public ApiResponse<PortfolioResponse> getPortfolioDetail(@PathVariable Long id) {
        return ApiResponse.ok(portfolioService.getPortfolioDetail(id, false));
    }
}
