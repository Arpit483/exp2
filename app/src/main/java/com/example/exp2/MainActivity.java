package com.example.exp2;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends AppCompatActivity {

    private String selectedRole = "";
    private Button studentButton, teacherButton, clubButton;
    private EditText emailEditText, passwordEditText;
    private CheckBox rememberMeCheckBox; // Added CheckBox
    private FirebaseAuth mAuth;
    private DatabaseReference database;

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
        TextView signUpTextView = findViewById(R.id.signUpTextView);
        rememberMeCheckBox = findViewById(R.id.rememberMeCheckBox); // Initialize CheckBox

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();

        loadSavedCredentials(); // Load saved credentials if any

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
                boolean rememberMe = rememberMeCheckBox.isChecked();

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
                                        database.child("users").child(user.getUid()).child("role")
                                                .addListenerForSingleValueEvent(new ValueEventListener() {
                                                    @Override
                                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                        String userRole = snapshot.getValue(String.class);

                                                        if (userRole != null && userRole.equals(selectedRole)) {
                                                            if (selectedRole.equals("Student")) {
                                                                Intent intent = new Intent(MainActivity.this, StudentActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else if (selectedRole.equals("Teacher")) {
                                                                Intent intent = new Intent(MainActivity.this, Teach_First.class);
                                                                startActivity(intent);
                                                                finish();
                                                            } else if (selectedRole.equals("Club")) {
                                                                Intent intent = new Intent(MainActivity.this, ClubHomeActivity.class);
                                                                startActivity(intent);
                                                                finish();
                                                            }
                                                        } else {
                                                            Toast.makeText(MainActivity.this, "Invalid role selection", Toast.LENGTH_SHORT).show();
                                                        }
                                                    }

                                                    @Override
                                                    public void onCancelled(@NonNull DatabaseError error) {
                                                        Toast.makeText(MainActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                                                    }
                                                });
                                    }
                                } else {
                                    Toast.makeText(MainActivity.this, "Authentication failed. " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                if (rememberMe) {
                    saveCredentials(email, password);
                } else {
                    clearCredentials();
                }
            }
        });

        signUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                startActivity(intent);
            }
        });
    }

    private void highlightButton(Button selectedButton) {
        studentButton.setBackgroundColor(Color.parseColor("#F9F9F9"));
        teacherButton.setBackgroundColor(Color.parseColor("#F9F9F9"));
        clubButton.setBackgroundColor(Color.parseColor("#F9F9F9"));
        selectedButton.setBackgroundColor(Color.parseColor("#E0E0E0"));
    }

    private void loadSavedCredentials() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        String savedEmail = preferences.getString("email", "");
        String savedPassword = preferences.getString("password", "");
        boolean rememberMe = preferences.getBoolean("rememberMe", false);

        emailEditText.setText(savedEmail);
        passwordEditText.setText(savedPassword);
        rememberMeCheckBox.setChecked(rememberMe);
    }

    private void saveCredentials(String email, String password) {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString("email", email);
        editor.putString("password", password);
        editor.putBoolean("rememberMe", true);
        editor.apply();
    }

    private void clearCredentials() {
        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor = preferences.edit();
        editor.remove("email");
        editor.remove("password");
        editor.remove("rememberMe");
        editor.apply();
    }
}