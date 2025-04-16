package com.example.exp2;

import retrofit2.Call; // Ensure this import is present
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import okhttp3.RequestBody;
import okhttp3.MultipartBody;

public interface YourApiService {
    @Multipart
    @POST("/mark-attendance-request")
    Call<AttendanceResponse> submitAttendanceRequest(
            @Part("roll_no") RequestBody rollNo,
            @Part("name") RequestBody name,
            @Part("subject") RequestBody subject,
            @Part("date") RequestBody date,
            @Part("reason") RequestBody reason,
            @Part MultipartBody.Part letter
    );
}