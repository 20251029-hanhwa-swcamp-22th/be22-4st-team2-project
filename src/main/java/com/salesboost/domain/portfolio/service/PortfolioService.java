package com.salesboost.domain.portfolio.service;

import com.salesboost.common.exception.BusinessException;
import com.salesboost.common.exception.ErrorCode;
import com.salesboost.domain.portfolio.dto.*;
import com.salesboost.domain.portfolio.entity.Portfolio;
import com.salesboost.domain.portfolio.entity.PortfolioImage;
import com.salesboost.domain.portfolio.repository.PortfolioRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getAdminPortfolios() {
        return portfolioRepository.findAllByOrderByDisplayOrderAscIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    public Long createPortfolio(PortfolioCreateRequest request) {
        String thumbnailUrl = (request.getThumbnailUrl() != null && !request.getThumbnailUrl().trim().isEmpty())
                ? request.getThumbnailUrl()
                : null;
        Portfolio portfolio = Portfolio.create(
                request.getTitle(),
                request.getDescription(),
                request.getClientName(),
                request.getIndustry(),
                thumbnailUrl
        );
        portfolio.updateVisibility(request.isVisible());
        Portfolio saved = portfolioRepository.save(portfolio);
        return saved.getId();
    }

    public void updatePortfolio(Long id, PortfolioUpdateRequest request) {
        Portfolio portfolio = findPortfolio(id);

        // 빈 문자열을 null로 처리하여 썸네일 URL 삭제 허용
        String thumbnailUrl = (request.getThumbnailUrl() != null && !request.getThumbnailUrl().trim().isEmpty())
                ? request.getThumbnailUrl()
                : null;

        portfolio.update(
                request.getTitle(),
                request.getDescription(),
                request.getClientName(),
                request.getIndustry(),
                thumbnailUrl
        );

        if (request.getVisible() != null) {
            portfolio.updateVisibility(request.getVisible());
        }
    }

    public void deletePortfolio(Long id) {
        Portfolio portfolio = findPortfolio(id);
        portfolioRepository.delete(portfolio);
    }

    public void updateVisibility(Long id, PortfolioVisibilityRequest request) {
        Portfolio portfolio = findPortfolio(id);
        portfolio.updateVisibility(request.getVisible());
    }

    public void updateOrder(PortfolioVisibilityOrderRequest request) {
        List<Long> ids = request.getPortfolioIds();
        if (ids == null || ids.isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REQUEST, "portfolioIds는 비어 있을 수 없습니다.");
        }

        int order = 1;
        for (Long id : ids) {
            Portfolio portfolio = findPortfolio(id);
            portfolio.updateDisplayOrder(order++);
        }
    }

    @Transactional(readOnly = true)
    public List<PortfolioResponse> getPublicPortfolios() {
        return portfolioRepository.findAllByVisibleTrueOrderByDisplayOrderAscIdDesc().stream()
                .map(this::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public PortfolioResponse getPortfolioDetail(Long id, boolean admin) {
        Portfolio portfolio = findPortfolio(id);
        if (!admin && !portfolio.isVisible()) {
            throw new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND);
        }
        return toResponse(portfolio);
    }

    private Portfolio findPortfolio(Long id) {
        return portfolioRepository.findById(id)
                .orElseThrow(() -> new BusinessException(ErrorCode.PORTFOLIO_NOT_FOUND));
    }

    private PortfolioResponse toResponse(Portfolio portfolio) {
        return new PortfolioResponse(
                portfolio.getId(),
                portfolio.getTitle(),
                portfolio.getDescription(),
                portfolio.getClientName(),
                portfolio.getIndustry(),
                portfolio.getThumbnailUrl(),
                portfolio.isVisible(),
                portfolio.getDisplayOrder(),
                portfolio.getImages().stream().map(PortfolioImage::getImageUrl).toList(),
                portfolio.getCreatedAt(),
                portfolio.getUpdatedAt()
        );
    }
}
