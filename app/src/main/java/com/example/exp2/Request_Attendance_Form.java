package com.example.exp2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.example.exp2.AttendanceResponse;
import com.example.exp2.FileUtils;
import com.example.exp2.R;
import com.google.android.material.textfield.TextInputEditText;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public class Request_Attendance_Form extends AppCompatActivity {

    private TextInputEditText rollNoEditText, nameEditText, subjectEditText, dateEditText, reasonEditText;
    private LinearLayout uploadArea;
    private TextView uploadTextView, fileNameTextView;
    private Button submitRequestButton;

    private static final int PICK_FILE_REQUEST = 1;
    private static final int STORAGE_PERMISSION_CODE = 101;
    private Uri fileUri;
    private File uploadedFile; // Store the File object
    private String filePath; // Declare filePath here

    private YourApiService apiService;

    private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_request_attendance_form);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        initializeViews();
        initializeRetrofit();
        setupListeners();
        checkPermissions();
    }

    private void initializeViews() {
        rollNoEditText = findViewById(R.id.rollNoEditText);
        nameEditText = findViewById(R.id.nameEditText);
        subjectEditText = findViewById(R.id.subjectEditText);
        dateEditText = findViewById(R.id.dateEditText);
        reasonEditText = findViewById(R.id.reasonEditText);
        uploadArea = findViewById(R.id.uploadArea);
        uploadTextView = findViewById(R.id.uploadTextView);
        fileNameTextView = findViewById(R.id.fileNameTextView);
        submitRequestButton = findViewById(R.id.submitRequestButton);

        // Set the input type to date for dateEditText
        dateEditText.setInputType(android.text.InputType.TYPE_CLASS_DATETIME | android.text.InputType.TYPE_DATETIME_VARIATION_DATE);
    }

    private void initializeRetrofit() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://9ed6-2409-40c2-104c-bae3-19d2-b49f-ec06-317a.ngrok-free.app/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();
        apiService = retrofit.create(YourApiService.class);
    }

    private void setupListeners() {
        uploadArea.setOnClickListener(v -> showFileChooser());
        submitRequestButton.setOnClickListener(v -> submitForm());
    }

    private void checkPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == STORAGE_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(this, "Storage Permission Granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Storage Permission Denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void showFileChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        try {
            startActivityForResult(Intent.createChooser(intent, "Select a File"), PICK_FILE_REQUEST);
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "Please install a File Manager.", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_FILE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            fileUri = data.getData();
            filePath = FileUtils.getFilePathFromContentUri(this, fileUri); // Use the improved method
            if (filePath != null) {
                uploadedFile = new File(filePath); // Store the File
                fileNameTextView.setText(uploadedFile.getName());
                fileNameTextView.setVisibility(View.VISIBLE);
                uploadTextView.setText("File Selected:");
            } else {
                Toast.makeText(this, "Could not retrieve file", Toast.LENGTH_SHORT).show();
                Log.e("FILE_UTIL", "Failed to get file path");
            }
        }
    }

    private void submitForm() {
        String rollNo = rollNoEditText.getText().toString().trim();
        String name = nameEditText.getText().toString().trim();
        String subject = subjectEditText.getText().toString().trim();
        String date = dateEditText.getText().toString().trim();
        String reason = reasonEditText.getText().toString().trim();

        if (!validateFields(rollNo, name, subject, date, reason)) {
            return; // Stop if validation fails
        }

        try {
            // Parse the date to ensure it's in the correct format
            dateFormat.parse(date);
        } catch (ParseException e) {
            Toast.makeText(this, "Invalid date format. Please use YYYY-MM-DD", Toast.LENGTH_SHORT).show();
            return;
        }


        RequestBody rollNoPart = createRequestBody(rollNo);
        RequestBody namePart = createRequestBody(name);
        RequestBody subjectPart = createRequestBody(subject);
        RequestBody datePart = createRequestBody(date);
        RequestBody reasonPart = createRequestBody(reason);

        MultipartBody.Part filePart = null;
        if (uploadedFile != null && filePath != null) { // Check if filePath is also not null
            filePart = createMultipartBodyPart("letter", uploadedFile);
        }

        sendAttendanceRequest(rollNoPart, namePart, subjectPart, datePart, reasonPart, filePart);
    }

    private boolean validateFields(String rollNo, String name, String subject, String date, String reason) {
        if (rollNo.isEmpty() || name.isEmpty() || subject.isEmpty() || date.isEmpty() || reason.isEmpty()) {
            Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private RequestBody createRequestBody(String text) {
        return RequestBody.create(MediaType.parse("text/plain"), text);
    }

    private MultipartBody.Part createMultipartBodyPart(String partName, File file) {
        RequestBody requestFile = RequestBody.create(MediaType.parse("multipart/form-data"), file);
        return MultipartBody.Part.createFormData(partName, file.getName(), requestFile);
    }

    private void sendAttendanceRequest(RequestBody rollNo, RequestBody name, RequestBody subject, RequestBody date, RequestBody reason, MultipartBody.Part letter) {
        Call<AttendanceResponse> call = apiService.submitAttendanceRequest(rollNo, name, subject, date, reason, letter); // Changed to AttendanceResponse
        call.enqueue(new Callback<AttendanceResponse>() {
            @Override
            public void onResponse(Call<AttendanceResponse> call, Response<AttendanceResponse> response) {
                if (response.isSuccessful()) {
                    handleSuccessResponse(response.body());
                } else {
                    handleErrorResponse(response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<AttendanceResponse> call, Throwable t) {
                handleFailure(t);
            }
        });
    }

    private void handleSuccessResponse(AttendanceResponse response) {
        if (response != null) {
            if (response.isSuccess()) {
                Log.i("API_SUCCESS", "Request successful: " + response.getMessage());
                Toast.makeText(Request_Attendance_Form.this, "Request submitted. Redirecting...", Toast.LENGTH_SHORT).show();

                //  ---  Navigation with Data Passing  ---
                Intent intent = new Intent(Request_Attendance_Form.this, StudentActivity.class);
                intent.putExtra("roll_number", rollNoEditText.getText().toString()); // Get roll number from the EditText
                intent.putExtra("request_date", dateEditText.getText().toString());   // Get date from the EditText
                startActivity(intent);
                finish();
                //  ---  End Navigation  ---

            } else {
                Log.e("API_ERROR", "Request failed (in success): " + response.getMessage());
                Toast.makeText(Request_Attendance_Form.this, "Request failed: " + response.getMessage(), Toast.LENGTH_SHORT).show();
            }
        } else {
            Log.e("API_ERROR", "Response body is null (but 200 OK)");
            Toast.makeText(Request_Attendance_Form.this, "Server error", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleErrorResponse(ResponseBody errorBody) {
        try {
            if (errorBody != null) {
                Log.e("API_ERROR", "Error Body: " + errorBody.string());
            } else {
                Log.e("API_ERROR", "Error Body is null");
            }
        } catch (IOException e) {
            Log.e("API_ERROR", "Error reading error body", e);
        }
        Toast.makeText(Request_Attendance_Form.this, "Failed to submit request", Toast.LENGTH_SHORT).show();
    }

    private void handleFailure(Throwable t) {
        Toast.makeText(Request_Attendance_Form.this, "Network error", Toast.LENGTH_SHORT).show();
        Log.e("API_ERROR", "Error: " + t.getMessage(), t);
    }

    private void clearForm() {
        rollNoEditText.setText("");
        nameEditText.setText("");
        subjectEditText.setText("");
        dateEditText.setText("");
        reasonEditText.setText("");
        uploadTextView.setText("Upload documents");
        fileNameTextView.setText("");
        fileNameTextView.setVisibility(View.GONE);
        fileUri = null;
        uploadedFile = null;
        filePath = null; // Also reset filePath
    }

    public interface YourApiService {
        @Multipart
        @POST("/mark-attendance-request")
        Call<AttendanceResponse> submitAttendanceRequest(  // Changed to AttendanceResponse
                                                           @Part("roll_no") RequestBody rollNo,
                                                           @Part("name") RequestBody name,
                                                           @Part("subject") RequestBody subject,
                                                           @Part("date") RequestBody date,
                                                           @Part("reason") RequestBody reason,
                                                           @Part MultipartBody.Part letter
        );
    }
}