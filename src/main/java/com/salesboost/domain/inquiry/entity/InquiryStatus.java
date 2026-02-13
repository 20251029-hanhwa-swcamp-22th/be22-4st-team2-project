package com.salesboost.domain.inquiry.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum InquiryStatus {

    PENDING("대기", "처리 대기 중"),
    IN_PROGRESS("진행중", "처리 진행 중"),
    DONE("완료", "처리 완료");

    private final String label;
    private final String description;
}
