package com.example.exp2;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
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

import com.example.exp2.recycler.Subject;
import com.example.exp2.recycler.SubjectAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class Teach_First extends AppCompatActivity {

    RecyclerView recyclerView;
    SubjectAdapter subjectAdapter;
    ArrayList<Subject> subjectList;
    Button addSubjectBtn;
    DatabaseReference databaseReference;
    String teacherUserId;
    TextView teacherNameTextView; // Add this

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_teach_first);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.mainLayout), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Button btnViewAttendance = findViewById(R.id.viewExcel);
        btnViewAttendance.setOnClickListener(v -> {
            Intent intent = new Intent(Teach_First.this, FileListActivity.class);
            startActivity(intent);
        });

        recyclerView = findViewById(R.id.subjectRecyclerView);
        addSubjectBtn = findViewById(R.id.addSubjectBtn);
        teacherNameTextView = findViewById(R.id.teacherNameTextView); // Initialize

        subjectList = new ArrayList<>();
        subjectAdapter = new SubjectAdapter(this, subjectList);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(subjectAdapter);

        // Get teacher's UID
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser != null) {
            teacherUserId = currentUser.getUid();
        } else {
            Toast.makeText(this, "Teacher not authenticated", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Use teacher's UID for database reference
        databaseReference = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUserId);

        // Get the teacher's name from the database
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    String firstName = dataSnapshot.child("firstName").getValue(String.class);
                    String lastName = dataSnapshot.child("lastName").getValue(String.class);
                    if (firstName != null && lastName != null) {
                        String teacherName = firstName + " " + lastName;
                        teacherNameTextView.setText(teacherName); // Set the teacher's name
                    } else if (firstName != null) {
                        teacherNameTextView.setText(firstName);
                    }
                } else {
                    teacherNameTextView.setText("Teacher"); // Default
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                teacherNameTextView.setText("Teacher"); // Default
                Toast.makeText(Teach_First.this, "Failed to load teacher name: " + databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

        databaseReference = FirebaseDatabase.getInstance().getReference("teachers").child(teacherUserId).child("subjects");
        // Firebase: Listen for existing subjects
        FirebaseUtil.listenToSubjects(subjectList, subjectAdapter, databaseReference);

        // Add subject button with input dialog
        addSubjectBtn.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Enter Subject Name");

            final EditText input = new EditText(this);
            input.setInputType(InputType.TYPE_CLASS_TEXT);
            builder.setView(input);

            builder.setPositiveButton("Add", (dialog, which) -> {
                String subjectName = input.getText().toString().trim();
                if (!subjectName.isEmpty()) {
                    String key = databaseReference.push().getKey();
                    if (key != null) {
                        Subject newSubject = new Subject(key, subjectName);
                        databaseReference.child(key).setValue(newSubject)
                                .addOnSuccessListener(aVoid -> Toast.makeText(this, "Subject added!", Toast.LENGTH_SHORT).show())
                                .addOnFailureListener(e -> Toast.makeText(this, "Failed to add subject", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    Toast.makeText(this, "Subject name cannot be empty", Toast.LENGTH_SHORT).show();
                }
            });

            builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }
}