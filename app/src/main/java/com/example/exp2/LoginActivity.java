package com.example.exp2;

import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText, rollNoEditText;
    private Button createAccountButton, studentButton, teacherButton, clubButton;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String selectedRole = "Student"; // Default role
    private TextInputLayout rollNoTextInputLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firstNameEditText = findViewById(R.id.firstNameEditText);
        lastNameEditText = findViewById(R.id.lastNameEditText);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        confirmPasswordEditText = findViewById(R.id.confirmPasswordEditText);
        createAccountButton = findViewById(R.id.createAccountButton);
        studentButton = findViewById(R.id.studentButton);
        teacherButton = findViewById(R.id.teacherButton);
        clubButton = findViewById(R.id.clubButton);
        rollNoEditText = findViewById(R.id.rollNoEditText);
        rollNoTextInputLayout = findViewById(R.id.rollNoTextInputLayout);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        highlightButton(studentButton); // Default highlight

        studentButton.setOnClickListener(v -> {
            selectedRole = "Student";
            rollNoTextInputLayout.setVisibility(View.VISIBLE);
            highlightButton(studentButton);
            unhighlightButton(teacherButton);
            unhighlightButton(clubButton);
        });

        teacherButton.setOnClickListener(v -> {
            selectedRole = "Teacher";
            rollNoTextInputLayout.setVisibility(View.GONE);
            highlightButton(teacherButton);
            unhighlightButton(studentButton);
            unhighlightButton(clubButton);
        });

        clubButton.setOnClickListener(v -> {
            selectedRole = "Club";
            rollNoTextInputLayout.setVisibility(View.GONE);
            highlightButton(clubButton);
            unhighlightButton(studentButton);
            unhighlightButton(teacherButton);
        });

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = firstNameEditText.getText().toString();
                final String lastName = lastNameEditText.getText().toString();
                final String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();
                String confirmPassword = confirmPasswordEditText.getText().toString();
                String rollNo = rollNoEditText.getText().toString();

                if (firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (!password.equals(confirmPassword)) {
                    Toast.makeText(LoginActivity.this, "Passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (selectedRole.equals("Student") && rollNo.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please enter Roll Number", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        database.child("users").child(user.getUid()).child("firstName").setValue(firstName);
                                        database.child("users").child(user.getUid()).child("lastName").setValue(lastName);
                                        database.child("users").child(user.getUid()).child("email").setValue(email);
                                        database.child("users").child(user.getUid()).child("role").setValue(selectedRole);

                                        if (selectedRole.equals("Teacher")) {
                                            DatabaseReference teacherRef = database.child("teachers").child(user.getUid());
                                            teacherRef.child("firstName").setValue(firstName);
                                            teacherRef.child("lastName").setValue(lastName);
                                            teacherRef.child("email").setValue(email);

                                            List<String> classes = new ArrayList<>();
                                            teacherRef.child("classes").setValue(classes);
                                        } else if (selectedRole.equals("Student")) {
                                            DatabaseReference studentRef = database.child("students").child(user.getUid());
                                            studentRef.child("firstName").setValue(firstName);
                                            studentRef.child("lastName").setValue(lastName);
                                            studentRef.child("email").setValue(email);
                                            studentRef.child("rollNo").setValue(rollNo);
                                        }

                                        Toast.makeText(LoginActivity.this, "Account created successfully.", Toast.LENGTH_SHORT).show();
                                        finish();
                                    }
                                } else {
                                    Toast.makeText(LoginActivity.this, "Authentication failed: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void highlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#3F51B5"));
        button.setTextColor(Color.WHITE);
    }

    private void unhighlightButton(Button button) {
        button.setBackgroundColor(Color.parseColor("#E8EAF6"));
        button.setTextColor(Color.parseColor("#3F51B5"));
    }
}