package com.salesboost.domain.inquiry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryType {

    PARTNERSHIP("제휴 문의", "일반 제휴 관련 문의"),
    DEMO_REQUEST("데모 요청", "제품 데모 시연 요청"),
    PRICE_INQUIRY("가격 문의", "가격 및 견적 관련 문의"),
    TECHNICAL("기술 지원", "기술적인 문제 및 지원 요청"),
    ETC("기타", "기타 문의 사항");

    private final String label;
    private final String description;
}
