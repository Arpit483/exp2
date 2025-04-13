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
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class LoginActivity extends AppCompatActivity {

    private EditText firstNameEditText, lastNameEditText, emailEditText, passwordEditText, confirmPasswordEditText;
    private Button createAccountButton, studentButton, teacherButton, clubButton;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private String selectedRole = "Student"; // Default role

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ... (findViewById and mAuth/database setup)

        createAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String firstName = firstNameEditText.getText().toString();
                final String lastName = lastNameEditText.getText().toString();
                final String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                // ... (input validation)

                mAuth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        // Store general user data
                                        database.child("users").child(user.getUid()).child("firstName").setValue(firstName);
                                        database.child("users").child(user.getUid()).child("lastName").setValue(lastName);
                                        database.child("users").child(user.getUid()).child("email").setValue(email);
                                        database.child("users").child(user.getUid()).child("role").setValue(selectedRole);

                                        if (selectedRole.equals("Teacher")) {
                                            // Store teacher-specific data
                                            DatabaseReference teacherRef = database.child("teachers").child(user.getUid());
                                            teacherRef.child("firstName").setValue(firstName);
                                            teacherRef.child("lastName").setValue(lastName);
                                            teacherRef.child("email").setValue(email);

                                            // Initialize an empty list of classes for THIS teacher
                                            List<String> classes = new ArrayList<>();
                                            teacherRef.child("classes").setValue(classes);
                                        }

                                        // ... (Toast and finish)
                                    }
                                } else {
                                    // ... (Toast error)
                                }
                            }
                        });
            }
        });

        // ... (highlightButton method)
    }
}