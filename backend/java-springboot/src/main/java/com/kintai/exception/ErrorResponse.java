package com.kintai.exception;

import java.util.Map;

public class ErrorResponse {
    private boolean success;
    private String errorCode;
    private String message;
    private Map<String, String> details;

    private ErrorResponse(Builder builder) {
        this.success = builder.success;
        this.errorCode = builder.errorCode;
        this.message = builder.message;
        this.details = builder.details;
    }

    public boolean isSuccess() { return success; }
    public String getErrorCode() { return errorCode; }
    public String getMessage() { return message; }
    public Map<String, String> getDetails() { return details; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private boolean success;
        private String errorCode;
        private String message;
        private Map<String, String> details;

        public Builder success(boolean success) { this.success = success; return this; }
        public Builder errorCode(String errorCode) { this.errorCode = errorCode; return this; }
        public Builder message(String message) { this.message = message; return this; }
        public Builder details(Map<String, String> details) { this.details = details; return this; }
        public ErrorResponse build() { return new ErrorResponse(this); }
    }
}



