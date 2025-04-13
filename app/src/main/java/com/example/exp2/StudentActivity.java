package com.example.exp2;

import android.os.Bundle;
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
    Button markAttendanceButton;
    String studentUserId;
    RecyclerView attendanceRecyclerView;

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

                    // Replace with your actual response retrieval logic (e.g., from server or Firebase)
                    String jsonResponse = "{\"attendance_summary\": [{\"attendance_percent\": 0.0, \"present_days\": 0, \"subject\": \"attendance_requests\", \"total_classes\": 4}, {\"attendance_percent\": 0.0, \"present_days\": 0, \"subject\": \"DSA_attendance\", \"total_classes\": 1}, {\"attendance_percent\": 28.57, \"present_days\": 2, \"subject\": \"OOP_attendance\", \"total_classes\": 7}, {\"attendance_percent\": 0.0, \"present_days\": 0, \"subject\": \"PPL_attendance\", \"total_classes\": 1}, {\"attendance_percent\": 0, \"present_days\": 0, \"subject\": \"students\", \"total_classes\": 0}, {\"attendance_percent\": 0.0, \"present_days\": 0, \"subject\": \"XYZ_attendance\", \"total_classes\": 2}], \"roll_no\": \"SCOB24\"}";

                    try {
                        JSONObject response = new JSONObject(jsonResponse);
                        JSONArray attendanceSummary = response.getJSONArray("attendance_summary");
                        AttendanceAdapter adapter = new AttendanceAdapter(attendanceSummary);
                        attendanceRecyclerView.setAdapter(adapter);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
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

        markAttendanceButton.setOnClickListener(v -> markAttendance());
    }

    private void markAttendance() {
        // Implement your attendance marking logic here (e.g., send data to Firebase)
        Toast.makeText(this, "Attendance Marked", Toast.LENGTH_SHORT).show();
    }
}