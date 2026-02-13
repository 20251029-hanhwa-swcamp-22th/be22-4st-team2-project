package com.salesboost.domain.inquiry.mapper;

import com.salesboost.domain.inquiry.dto.InquiryListItemResponse;
import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface InquiryQueryMapper {

    List<InquiryListItemResponse> findInquiries(
            @Param("status") String status,
            @Param("keyword") String keyword,
            @Param("sort") String sort,
            @Param("limit") int limit,
            @Param("offset") int offset
    );

    long countInquiries(
            @Param("status") String status,
            @Param("keyword") String keyword
    );
}
