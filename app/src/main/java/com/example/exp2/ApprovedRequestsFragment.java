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
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.JsonArrayRequest;
import com.android.volley.toolbox.Volley;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

public class ApprovedRequestsFragment extends Fragment {

    private RecyclerView recyclerView;
    private ClubAdapter clubAdapter;
    private List<Club> clubList;
    private RequestQueue requestQueue;
    private String attendanceRequestsUrl = "https://9ed6-2409-40c2-104c-bae3-19d2-b49f-ec06-317a.ngrok-free.app/attendance-requests";

    public ApprovedRequestsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_approved_requests, container, false);
        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        clubList = new ArrayList<>();
        clubAdapter = new ClubAdapter(clubList);
        recyclerView.setAdapter(clubAdapter);
        requestQueue = Volley.newRequestQueue(getContext());
        fetchAttendanceRequests("Approved"); // Fetch only approved requests
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
}