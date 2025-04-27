package com.example.exp2; // Replace with your actual package name

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PendingRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClubAdapter clubAdapter;
    private List<Club> clubList;
    private RequestQueue requestQueue;
    private String attendanceRequestsUrl = "https://4290-2409-40c2-1033-4171-6506-37e3-1918-77ce.ngrok-free.app/attendance-requests";

    public PendingRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_pending_requests, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        clubList = new ArrayList<>();
        clubAdapter = new ClubAdapter(clubList);
        recyclerView.setAdapter(clubAdapter);
        requestQueue = Volley.newRequestQueue(getContext());
        fetchAttendanceRequests("Pending"); // Fetch only pending requests
        clubAdapter.setOnItemClickListener(new ClubAdapter.OnItemClickListener() {
            @Override
            public void onApproveClick(int position, Club club) {
                updateAttendanceStatus(club.getRollNo(), club.getDate(), "Approved");
            }

            @Override
            public void onRejectClick(int position, Club club) {
                updateAttendanceStatus(club.getRollNo(), club.getDate(), "Rejected");
            }
        });
        return view;
    }

    private void fetchAttendanceRequests(String statusFilter) {
        JsonArrayRequest jsonArrayRequest = new JsonArrayRequest(Request.Method.GET, attendanceRequestsUrl, null,
                response -> {
                    try {
                        clubList.clear();
                        for (int i = 0; i < response.length(); i++) {
                            JSONObject request = response.getJSONObject(i);
                            if (request.getString("status").equals(statusFilter)) {
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
                        }
                        clubAdapter.notifyDataSetChanged();
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error parsing JSON", Toast.LENGTH_SHORT).show();
                    }
                }, error -> {
            error.printStackTrace();
            Toast.makeText(getContext(), "Error fetching data", Toast.LENGTH_SHORT).show();
        });
        requestQueue.add(jsonArrayRequest);
    }

    private void updateAttendanceStatus(String rollNo, String date, String newStatus) {
        String updateUrl = "https://4290-2409-40c2-1033-4171-6506-37e3-1918-77ce.ngrok-free.app/attendance-requests/" + rollNo + "/" + date;

        JSONObject jsonParams = new JSONObject();
        try {
            jsonParams.put("status", newStatus);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        JsonObjectRequest request = new JsonObjectRequest(Request.Method.PATCH, updateUrl, jsonParams,
                response -> {
                    try {
                        String message = response.getString("message");
                        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
                        fetchAttendanceRequests("Pending"); // Refresh pending list
                    } catch (JSONException e) {
                        Toast.makeText(getContext(), "Status updated successfully", Toast.LENGTH_SHORT).show();
                        fetchAttendanceRequests("Pending");
                        e.printStackTrace();
                    }
                }, error -> {
            Toast.makeText(getContext(), "Error updating status: " + error.toString(), Toast.LENGTH_LONG).show();
            error.printStackTrace();
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        requestQueue.add(request);
    }
}