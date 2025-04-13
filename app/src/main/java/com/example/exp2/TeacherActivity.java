package com.example.exp2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeacherActivity extends AppCompatActivity {

    Button camOnBtn, camOffBtn, addFaceBtn;
    ProgressBar progressBar;
    TextView tvResponse;
    String subjectName;
    String teacherUserId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teacher);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        subjectName = getIntent().getStringExtra("subjectName");
        if (subjectName == null) subjectName = "Unknown";

        camOnBtn = findViewById(R.id.camOn);
        camOffBtn = findViewById(R.id.CamOff);
        addFaceBtn = findViewById(R.id.AddFace);
        progressBar = findViewById(R.id.progressBar);
        tvResponse = findViewById(R.id.tvResponse);

        // Get teacher's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            teacherUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Teacher not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Get teacher's name from Firebase
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUserId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String teacherName;
                    if (firstName != null && lastName != null) {
                        teacherName = firstName + " " + lastName;
                    } else if (firstName != null) {
                        teacherName = firstName;
                    } else {
                        teacherName = "Teacher"; // Default
                    }

                    // Set teacher's name in TextView
                    TextView teacherNameTextView = findViewById(R.id.teacherName);
                    teacherNameTextView.setText(teacherName);
                } else {
                    TextView teacherNameTextView = findViewById(R.id.teacherName);
                    teacherNameTextView.setText("Teacher"); // Default if not found
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                TextView teacherNameTextView = findViewById(R.id.teacherName);
                teacherNameTextView.setText("Teacher"); // Default on error
                Toast.makeText(TeacherActivity.this, "Failed to load teacher name: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        camOnBtn.setOnClickListener(v -> sendCameraOnRequest());
        camOffBtn.setOnClickListener(v -> sendCameraOffRequest());
        addFaceBtn.setOnClickListener(v -> showAddFaceDialog());
    }

    private void showAddFaceDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_face, null);
        EditText etRollNo = dialogView.findViewById(R.id.etRollNo);
        EditText etName = dialogView.findViewById(R.id.etName);
        Button submitBtn = dialogView.findViewById(R.id.submitBtn);

        builder.setView(dialogView);
        AlertDialog dialog = builder.create();
        dialog.setTitle("Add Student Face");

        submitBtn.setOnClickListener(v -> {
            String rollNo = etRollNo.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (!rollNo.isEmpty() && !name.isEmpty()) {
                dialog.dismiss();
                sendAddFaceRequest(rollNo, name);
            } else {
                Toast.makeText(this, "Please enter both fields", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void sendCameraOnRequest() {
        sendPostRequest("https://0bf1-2409-40c2-105f-6fa4-6171-ab5e-5c81-f784.ngrok-free.app/camera-on", "Camera On", "{\"subject\": \"" + subjectName + "\"}");
    }

    private void sendCameraOffRequest() {
        sendPostRequest("https://0bf1-2409-40c2-105f-6fa4-6171-ab5e-5c81-f784.ngrok-free.app/camera-off", "Camera Off", "{\"subject\": \"" + subjectName + "\"}");
    }

    private void sendAddFaceRequest(String rollNo, String name) {
        String json = "{\"roll_no\": \"" + rollNo + "\", \"name\": \"" + name + "\"}";
        sendPostRequest("https://0bf1-2409-40c2-105f-6fa4-6171-ab5e-5c81-f784.ngrok-free.app/add-face", "Add Face", json);
    }

    private void sendPostRequest(String urlString, String actionName, String jsonInputString) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.VISIBLE);
            tvResponse.setText("Processing...");
            disableButtons();
        });

        new Thread(() -> {
            try {
                URL url = new URL(urlString);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json; utf-8");
                conn.setRequestProperty("Accept", "application/json");
                conn.setDoOutput(true);

                try (OutputStream os = conn.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes("utf-8");
                    os.write(input, 0, input.length);
                }

                int code = conn.getResponseCode();
                BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "utf-8"));
                StringBuilder response = new StringBuilder();
                String responseLine;
                while ((responseLine = br.readLine()) != null) {
                    response.append(responseLine.trim());
                }

                handleApiResponse(actionName, code, response.toString());

            } catch (Exception e) {
                Log.e("CAMERA_API", "Error: " + e.getMessage(), e);
                handleApiError(actionName, e.getMessage());
            }
        }).start();
    }

    private void handleApiResponse(String actionName, int code, String responseString) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            enableButtons();
            try {
                JSONObject response = new JSONObject(responseString);
                String message = response.optString("message", "Success");
                tvResponse.setText(message);
                Toast.makeText(this, actionName + " Success! Code: " + code, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                tvResponse.setText("Response: " + responseString);
                Toast.makeText(this, actionName + " Success! Code: " + code, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void handleApiError(String actionName, String errorMessage) {
        runOnUiThread(() -> {
            progressBar.setVisibility(View.GONE);
            enableButtons();
            tvResponse.setText(actionName + " Error: " + errorMessage);
            Snackbar.make(findViewById(R.id.main), actionName + " Failed: " + errorMessage, Snackbar.LENGTH_LONG).show();
        });
    }

    private void disableButtons() {
        camOnBtn.setEnabled(false);
        camOffBtn.setEnabled(false);
        addFaceBtn.setEnabled(false);
    }

    private void enableButtons() {
        camOnBtn.setEnabled(true);
        camOffBtn.setEnabled(true);
        addFaceBtn.setEnabled(true);
    }
}