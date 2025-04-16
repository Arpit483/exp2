package com.example.exp2;

public class AttendanceResponse {
    private String message;
    private boolean success;
    // Add other fields if your JSON response has more (e.g., an ID)

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    // Add getters and setters for other fields if needed
}