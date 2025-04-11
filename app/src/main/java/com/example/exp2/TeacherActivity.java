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
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class TeacherActivity extends AppCompatActivity {

    Button camOnBtn, camOffBtn, addFaceBtn ;
    ProgressBar progressBar;
    TextView tvResponse;
    String subjectName;

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

        camOnBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            tvResponse.setText("");
            new Thread(this::sendCameraOnRequest).start();
        });

        camOffBtn.setOnClickListener(v -> {
            progressBar.setVisibility(View.VISIBLE);
            tvResponse.setText("");
            new Thread(this::sendCameraOffRequest).start();
        });

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

        submitBtn.setOnClickListener(v -> {
            String rollNo = etRollNo.getText().toString().trim();
            String name = etName.getText().toString().trim();

            if (!rollNo.isEmpty() && !name.isEmpty()) {
                dialog.dismiss();
                progressBar.setVisibility(View.VISIBLE);
                new Thread(() -> sendAddFaceRequest(rollNo, name)).start();
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

            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvResponse.setText("Response: " + response);
                Toast.makeText(this, actionName + " Success! Code: " + code, Toast.LENGTH_SHORT).show();
            });

        } catch (Exception e) {
            Log.e("CAMERA_API", "Error: " + e.getMessage(), e);
            runOnUiThread(() -> {
                progressBar.setVisibility(View.GONE);
                tvResponse.setText(actionName + " Error: " + e.getMessage());
                Toast.makeText(this, actionName + " Failed", Toast.LENGTH_SHORT).show();
            });
        }
    }
}
