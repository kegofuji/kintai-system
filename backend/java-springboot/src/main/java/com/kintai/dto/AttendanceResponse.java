package com.kintai.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AttendanceResponse {
    
    private boolean success;
    private Object data;
    private String message;
    private String errorCode;
    private List<String> details;
    
    // Constructors
    public AttendanceResponse() {}
    
    public AttendanceResponse(boolean success, Object data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }
    
    // Static factory methods
    public static AttendanceResponse success(Object data, String message) {
        return new AttendanceResponse(true, data, message);
    }
    
    public static AttendanceResponse success(Object data) {
        return new AttendanceResponse(true, data, null);
    }
    
    public static AttendanceResponse success(String message) {
        return new AttendanceResponse(true, null, message);
    }
    
    public static AttendanceResponse error(String errorCode, String message) {
        AttendanceResponse response = new AttendanceResponse(false, null, message);
        response.setErrorCode(errorCode);
        return response;
    }
    
    public static AttendanceResponse error(String errorCode, String message, List<String> details) {
        AttendanceResponse response = error(errorCode, message);
        response.setDetails(details);
        return response;
    }
    
    // Getters and Setters
    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public List<String> getDetails() { return details; }
    public void setDetails(List<String> details) { this.details = details; }
}
