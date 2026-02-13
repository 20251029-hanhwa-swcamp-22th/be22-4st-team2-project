package com.salesboost.domain.inquiry.dto;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class InquiryListResponse {
    private List<InquiryListItemResponse> items;
    private long totalCount;
    private int page;
    private int size;
}
