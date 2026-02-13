package com.salesboost.domain.inquiry.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "inquiries")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String companyName;

    @Column(nullable = false, length = 50)
    private String contactName;

    @Column(nullable = false, length = 100)
    private String email;

    @Column(nullable = false, length = 20)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryType inquiryType;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private InquiryStatus status = InquiryStatus.PENDING;

    @Column(columnDefinition = "TEXT")
    private String adminMemo;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;

    @Builder
    public Inquiry(String companyName, String contactName, String email, String phone,
                   InquiryType inquiryType, String content) {
        this.companyName = companyName;
        this.contactName = contactName;
        this.email = email;
        this.phone = phone;
        this.inquiryType = inquiryType;
        this.content = content;
        this.status = InquiryStatus.PENDING;
    }

    public void updateStatus(InquiryStatus status) {
        this.status = status;
    }

    public void updateMemo(String memo) {
        this.adminMemo = memo;
    }
}
