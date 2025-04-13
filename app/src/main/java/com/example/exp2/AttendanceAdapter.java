package com.example.exp2;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.ViewHolder> {

    private JSONArray attendanceSummary;

    public AttendanceAdapter(JSONArray attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        try {
            JSONObject item = attendanceSummary.getJSONObject(position);
            holder.subjectTextView.setText("Subject: " + item.getString("subject"));
            holder.presentDaysTextView.setText("Present Days: " + item.getInt("present_days"));
            holder.totalClassesTextView.setText("Total Classes: " + item.getInt("total_classes"));
            holder.attendancePercentTextView.setText("Attendance: " + item.getDouble("attendance_percent") + "%");
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return attendanceSummary.length();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView, presentDaysTextView, totalClassesTextView, attendancePercentTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            presentDaysTextView = itemView.findViewById(R.id.presentDaysTextView);
            totalClassesTextView = itemView.findViewById(R.id.totalClassesTextView);
            attendancePercentTextView = itemView.findViewById(R.id.attendancePercentTextView);
        }
    }
}