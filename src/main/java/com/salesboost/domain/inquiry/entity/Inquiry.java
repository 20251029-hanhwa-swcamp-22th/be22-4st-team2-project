package com.salesboost.domain.inquiry.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "inquiry")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Inquiry {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 150)
    private String companyName;

    @Column(nullable = false, length = 100)
    private String contactName;

    @Column(nullable = false, length = 150)
    private String email;

    @Column(nullable = false, length = 30)
    private String phone;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InquiryType inquiryType;

    @Column(nullable = false, length = 3000)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InquiryStatus status;

    @Column(length = 3000)
    private String adminMemo;

    @Column(nullable = false)
    private LocalDateTime createdAt;

    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = this.createdAt;
        if (this.status == null) {
            this.status = InquiryStatus.PENDING;
        }
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public void changeStatus(InquiryStatus status) {
        this.status = status;
    }

    public void changeAdminMemo(String memo) {
        this.adminMemo = memo;
    }

    @Builder
    public static Inquiry create(String companyName, String contactName, String email,
                                  String phone, InquiryType inquiryType, String content) {
        Inquiry inquiry = new Inquiry();
        inquiry.companyName = companyName;
        inquiry.contactName = contactName;
        inquiry.email = email;
        inquiry.phone = phone;
        inquiry.inquiryType = inquiryType;
        inquiry.content = content;
        inquiry.status = InquiryStatus.PENDING;
        return inquiry;
    }
}
