package com.salesboost.domain.inquiry.mapper;

import com.salesboost.domain.inquiry.dto.InquiryListItemResponse;
import com.salesboost.domain.inquiry.entity.InquiryStatus;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface InquiryQueryMapper {

    /**
     * 문의 목록 조회 (필터링, 검색, 페이징)
     */
    List<InquiryListItemResponse> findInquiries(
            @Param("status") InquiryStatus status,
            @Param("keyword") String keyword,
            @Param("offset") int offset,
            @Param("size") int size
    );

    /**
     * 문의 전체 개수 조회 (필터링, 검색 포함)
     */
    long countInquiries(
            @Param("status") InquiryStatus status,
            @Param("keyword") String keyword
    );
}
