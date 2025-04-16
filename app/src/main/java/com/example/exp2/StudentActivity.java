package com.example.exp2;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class StudentActivity extends AppCompatActivity {

    TextView studentNameTextView, studentRollNoTextView;
    String studentUserId;
    RecyclerView attendanceRecyclerView;
    RequestQueue requestQueue;
    Button requestAttendanceButton; // Added button

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_student);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.student_main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        studentNameTextView = findViewById(R.id.studentName);
        studentRollNoTextView = findViewById(R.id.studentRollNo);
        attendanceRecyclerView = findViewById(R.id.attendanceRecyclerView);
        requestAttendanceButton = findViewById(R.id.bottomButton); // Initialize the button

        attendanceRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            studentUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Student not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("students").child(studentUserId);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    String rollNo = dataSnapshot.child("rollNo").getValue(String.class);

                    String studentName = (firstName != null && lastName != null) ? firstName + " " + lastName : (firstName != null) ? firstName : "Student";
                    studentNameTextView.setText(studentName);
                    studentRollNoTextView.setText("Roll No: " + rollNo);

                    // Fetch attendance data from the URL using the rollNo from Firebase
                    fetchAttendanceData(rollNo);

                    // Set onClick listener for the request attendance button
                    requestAttendanceButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent intent = new Intent(StudentActivity.this, Request_Attendance_Form.class);
                            startActivity(intent);
                        }
                    });

                } else {
                    studentNameTextView.setText("Student");
                    studentRollNoTextView.setText("Roll No: Unknown");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                studentNameTextView.setText("Student");
                studentRollNoTextView.setText("Roll No: Unknown");
                Toast.makeText(StudentActivity.this, "Failed to load student data: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchAttendanceData(String rollNo) {
        String url = "https://9ed6-2409-40c2-104c-bae3-19d2-b49f-ec06-317a.ngrok-free.app/student-attendance/" + rollNo; // Append rollNo to URL
        requestQueue = Volley.newRequestQueue(this);

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null, new Response.Listener<JSONObject>() {
            @Override
            public void onResponse(JSONObject response) {
                try {
                    JSONArray attendanceSummary = response.getJSONArray("attendance_summary");
                    AttendanceAdapter adapter = new AttendanceAdapter(attendanceSummary);
                    attendanceRecyclerView.setAdapter(adapter);
                } catch (JSONException e) {
                    e.printStackTrace();
                    Toast.makeText(StudentActivity.this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                }
            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                error.printStackTrace();
                Toast.makeText(StudentActivity.this, "Error fetching data", Toast.LENGTH_SHORT).show();
            }
        });
        requestQueue.add(jsonObjectRequest);
    }
}