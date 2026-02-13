package com.salesboost.domain.portfolio.service;

import com.salesboost.domain.portfolio.dto.PortfolioCreateRequest;
import com.salesboost.domain.portfolio.dto.PortfolioResponse;
import com.salesboost.domain.portfolio.dto.PortfolioUpdateRequest;
import com.salesboost.domain.portfolio.entity.Portfolio;
import com.salesboost.domain.portfolio.entity.PortfolioImage;
import com.salesboost.domain.portfolio.repository.PortfolioImageRepository;
import com.salesboost.domain.portfolio.repository.PortfolioRepository;
import com.salesboost.domain.portfolio.service.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PortfolioService {

    private final PortfolioRepository portfolioRepository;
    private final PortfolioImageRepository portfolioImageRepository;
    private final FileStorageService fileStorageService;

    /**
     * 노출 중인 포트폴리오 목록 조회 (Public API)
     */
    public List<PortfolioResponse> getVisiblePortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findByIsVisibleTrueOrderByDisplayOrderAsc();
        return portfolios.stream()
                .map(PortfolioResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 상세 조회 (Public API)
     */
    public PortfolioResponse getPortfolioById(Long id) {
        Portfolio portfolio = portfolioRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. ID: " + id));

        if (!portfolio.getIsVisible()) {
            throw new IllegalArgumentException("비공개 포트폴리오입니다.");
        }

        return PortfolioResponse.from(portfolio);
    }

    /**
     * 전체 포트폴리오 목록 조회 (Admin API)
     */
    public List<PortfolioResponse> getAllPortfolios() {
        List<Portfolio> portfolios = portfolioRepository.findAllByOrderByDisplayOrderAsc();
        return portfolios.stream()
                .map(PortfolioResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 포트폴리오 등록 (Admin API)
     */
    @Transactional
    public Long createPortfolio(PortfolioCreateRequest request) {
        // 썸네일 업로드
        String thumbnailUrl = null;
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            thumbnailUrl = fileStorageService.store(request.getThumbnail());
        }

        // 포트폴리오 저장
        Portfolio portfolio = request.toEntity(thumbnailUrl);
        Portfolio savedPortfolio = portfolioRepository.save(portfolio);

        // 이미지 업로드 및 저장
        if (request.getImages() != null && !request.getImages().isEmpty()) {
            int order = 0;
            for (MultipartFile imageFile : request.getImages()) {
                if (!imageFile.isEmpty()) {
                    String imageUrl = fileStorageService.store(imageFile);
                    PortfolioImage image = PortfolioImage.builder()
                            .portfolio(savedPortfolio)
                            .imageUrl(imageUrl)
                            .displayOrder(order++)
                            .build();
                    savedPortfolio.addImage(image);
                }
            }
        }

        log.info("포트폴리오 등록 완료 - ID: {}, 제목: {}", savedPortfolio.getId(), savedPortfolio.getTitle());
        return savedPortfolio.getId();
    }

    /**
     * 포트폴리오 수정 (Admin API)
     */
    @Transactional
    public void updatePortfolio(Long id, PortfolioUpdateRequest request) {
        Portfolio portfolio = portfolioRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. ID: " + id));

        // 썸네일 업로드
        String thumbnailUrl = portfolio.getThumbnailUrl();
        if (request.getThumbnail() != null && !request.getThumbnail().isEmpty()) {
            if (thumbnailUrl != null) {
                fileStorageService.delete(thumbnailUrl);
            }
            thumbnailUrl = fileStorageService.store(request.getThumbnail());
        }

        // 포트폴리오 정보 수정
        portfolio.update(request.getTitle(), request.getDescription(),
                request.getClient(), request.getIndustry(), thumbnailUrl);

        // 이미지 삭제
        if (request.getDeleteImageIds() != null && !request.getDeleteImageIds().isEmpty()) {
            for (Long imageId : request.getDeleteImageIds()) {
                PortfolioImage image = portfolioImageRepository.findById(imageId)
                        .orElseThrow(() -> new IllegalArgumentException("이미지를 찾을 수 없습니다. ID: " + imageId));
                fileStorageService.delete(image.getImageUrl());
                portfolio.removeImage(image);
                portfolioImageRepository.delete(image);
            }
        }

        // 새 이미지 추가
        if (request.getNewImages() != null && !request.getNewImages().isEmpty()) {
            int currentMaxOrder = portfolio.getImages().stream()
                    .mapToInt(PortfolioImage::getDisplayOrder)
                    .max()
                    .orElse(-1);

            int order = currentMaxOrder + 1;
            for (MultipartFile imageFile : request.getNewImages()) {
                if (!imageFile.isEmpty()) {
                    String imageUrl = fileStorageService.store(imageFile);
                    PortfolioImage image = PortfolioImage.builder()
                            .portfolio(portfolio)
                            .imageUrl(imageUrl)
                            .displayOrder(order++)
                            .build();
                    portfolio.addImage(image);
                }
            }
        }

        log.info("포트폴리오 수정 완료 - ID: {}", id);
    }

    /**
     * 포트폴리오 삭제 (Admin API)
     */
    @Transactional
    public void deletePortfolio(Long id) {
        Portfolio portfolio = portfolioRepository.findByIdWithImages(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. ID: " + id));

        // 썸네일 삭제
        if (portfolio.getThumbnailUrl() != null) {
            fileStorageService.delete(portfolio.getThumbnailUrl());
        }

        // 이미지 삭제
        for (PortfolioImage image : portfolio.getImages()) {
            fileStorageService.delete(image.getImageUrl());
        }

        portfolioRepository.delete(portfolio);
        log.info("포트폴리오 삭제 완료 - ID: {}", id);
    }

    /**
     * 노출 여부 변경 (Admin API)
     */
    @Transactional
    public void updateVisibility(Long id, Boolean isVisible) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. ID: " + id));

        portfolio.updateVisibility(isVisible);
        log.info("포트폴리오 노출 여부 변경 - ID: {}, 노출: {}", id, isVisible);
    }

    /**
     * 순서 변경 (Admin API)
     */
    @Transactional
    public void updateDisplayOrder(Long id, Integer displayOrder) {
        Portfolio portfolio = portfolioRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("포트폴리오를 찾을 수 없습니다. ID: " + id));

        portfolio.updateDisplayOrder(displayOrder);
        log.info("포트폴리오 순서 변경 - ID: {}, 순서: {}", id, displayOrder);
    }
}
