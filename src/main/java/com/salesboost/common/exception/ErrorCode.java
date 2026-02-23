package com.salesboost.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST(HttpStatus.BAD_REQUEST, "C400", "잘못된 요청입니다."),
    INQUIRY_NOT_FOUND(HttpStatus.NOT_FOUND, "I404", "문의를 찾을 수 없습니다."),
    PORTFOLIO_NOT_FOUND(HttpStatus.NOT_FOUND, "P404", "포트폴리오를 찾을 수 없습니다."),
    INVALID_REFRESH_TOKEN(HttpStatus.UNAUTHORIZED, "A401", "유효하지 않거나 만료된 리프레시 토큰입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C500", "서버 내부 오류가 발생했습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }
}
