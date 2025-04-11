package com.example.exp2;

import android.content.Intent;
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

public class MainActivity extends AppCompatActivity {

    private String selectedRole = "";
    private Button studentButton, teacherButton, clubButton;
    private EditText emailEditText, passwordEditText;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button signInButton = findViewById(R.id.signInButton);
        studentButton = findViewById(R.id.button);
        teacherButton = findViewById(R.id.button2);
        clubButton = findViewById(R.id.button3);
        emailEditText = findViewById(R.id.emailEditText);
        passwordEditText = findViewById(R.id.passwordEditText);

        mAuth = FirebaseAuth.getInstance();

        studentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "Student";
                highlightButton(studentButton);
            }
        });

        teacherButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "Teacher";
                highlightButton(teacherButton);
            }
        });

        clubButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedRole = "Club";
                highlightButton(clubButton);
            }
        });

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = emailEditText.getText().toString();
                String password = passwordEditText.getText().toString();

                if (email.isEmpty() || password.isEmpty() || selectedRole.isEmpty()) {
                    Toast.makeText(MainActivity.this, "Please fill all fields and select a role", Toast.LENGTH_SHORT).show();
                    return;
                }

                mAuth.signInWithEmailAndPassword(email, password)
                        .addOnCompleteListener(MainActivity.this, new OnCompleteListener<AuthResult>() {
                            @Override
                            public void onComplete(@NonNull Task<AuthResult> task) {
                                if (task.isSuccessful()) {
                                    FirebaseUser user = mAuth.getCurrentUser();
                                    if (user != null) {
                                        if (selectedRole.equals("Student")) {
                                            Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (selectedRole.equals("Teacher")) {
                                            Intent intent = new Intent(MainActivity.this, Teach_First.class);
                                            startActivity(intent);
                                            finish();
                                        } else if (selectedRole.equals("Club")) {
                                            Toast.makeText(MainActivity.this, "Club Activity not implemented", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
            }
        });
    }

    private void highlightButton(Button selectedButton) {
        studentButton.setBackgroundColor(Color.parseColor("#F9F9F9"));
        teacherButton.setBackgroundColor(Color.parseColor("#F9F9F9"));
        clubButton.setBackgroundColor(Color.parseColor("#F9F9F9"));
        selectedButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
    }
}