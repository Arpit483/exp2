package com.example.exp2; // Replace with your actual package name

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.StringRequest;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ClubActivity extends AppCompatActivity {

    private RecyclerView clubRecyclerView;
    private ClubAdapter clubAdapter;
    private List<Club> clubList;
    private RequestQueue requestQueue;
    private String attendanceRequestsUrl = "https://4290-2409-40c2-1033-4171-6506-37e3-1918-77ce.ngrok-free.app/attendance-requests";
    private String updateApiEndpoint = "https://4290-2409-40c2-1033-4171-6506-37e3-1918-77ce.ngrok-free.app/attendance-requests"; // Use the full URL for update
    private TextView facultyNameTextView;
    private ImageView profileImageView;
    private FirebaseAuth mAuth;
    private DatabaseReference database;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_club);

        clubRecyclerView = findViewById(R.id.clubRecyclerView);
        facultyNameTextView = findViewById(R.id.facultyNameTextView);
        profileImageView = findViewById(R.id.profileImageView);

        clubRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        clubList = new ArrayList<>();
        clubAdapter = new ClubAdapter(clubList);
        clubRecyclerView.setAdapter(clubAdapter);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance().getReference();
        currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            fetchUserNameFromFirebase(currentUser.getUid());
        } else {
            facultyNameTextView.setText("Club Official");
        }

        requestQueue = Volley.newRequestQueue(this);
        fetchAttendanceRequests();

        clubAdapter.setOnItemClickListener(new ClubAdapter.OnItemClickListener() {
            @Override
            public void onApproveClick(int position, Club club) {
                updateAttendanceStatus(club.getRollNo(), club.getSubject(), "Approved");
            }

            @Override
            public void onRejectClick(int position, Club club) {
                updateAttendanceStatus(club.getRollNo(), club.getSubject(), "Rejected");
            }
        });
    }

    private void fetchUserNameFromFirebase(String userId) {
        database.child("users").child(userId).child("name")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        String name = snapshot.getValue(String.class);
                        if (name != null) {
                            facultyNameTextView.setText(name);
                        } else {
                            facultyNameTextView.setText("Club Official");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Toast.makeText(ClubActivity.this, "Error fetching user name: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        facultyNameTextView.setText("Club Official");
                    }
                });
    }

    private void fetchAttendanceRequests() {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, attendanceRequestsUrl, null,
                response -> {
                    try {
                        clubList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject request = response.getJSONObject(i);
                            Club club = new Club(
                                    request.getString("date"),
                                    request.getString("letter_path"),
                                    request.getString("letter_url"),
                                    request.getString("name"),
                                    request.getString("reason"),
                                    request.getString("roll_no"),
                                    request.getString("status"),
                                    request.getString("subject")
                            );
                            clubList.add(club);
                        }
                        clubAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Error parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            error.printStackTrace();
            Toast.makeText(this, "Error fetching data", Toast.LENGTH_SHORT).show();
        });

        requestQueue.add(jsonArrayRequest);
    }

    private void updateAttendanceStatus(String rollNo, String subject, String newStatus) {
        StringRequest stringRequest = new StringRequest(Request.Method.POST, updateApiEndpoint,
                response -> {
                    Toast.makeText(ClubActivity.this, "Status updated successfully", Toast.LENGTH_SHORT).show();
                    fetchAttendanceRequests(); // Refresh the list after update
                }, error -> {
            Toast.makeText(ClubActivity.this, "Error updating status", Toast.LENGTH_SHORT).show();
            error.printStackTrace();
        }) {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();
                params.put("roll_no", rollNo);
                params.put("subject", subject);
                params.put("status", newStatus);
                return params;
            }
        };

        requestQueue.add(stringRequest);
    }
}