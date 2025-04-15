package com.example.exp2;

import android.graphics.Color;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Locale;

public class AttendanceAdapter extends RecyclerView.Adapter<AttendanceAdapter.AttendanceViewHolder> {

    private JSONArray attendanceSummary;

    public AttendanceAdapter(JSONArray attendanceSummary) {
        this.attendanceSummary = attendanceSummary;
    }

    @NonNull
    @Override
    public AttendanceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.attendance_item, parent, false);
        return new AttendanceViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AttendanceViewHolder holder, int position) {
        try {
            JSONObject attendanceItem = attendanceSummary.getJSONObject(position);
            String subject = attendanceItem.getString("subject");
            double attendancePercent = attendanceItem.getDouble("attendance_percent");
            int presentDays = attendanceItem.getInt("present_days");
            int totalClasses = attendanceItem.getInt("total_classes");
            int absentDays = totalClasses - presentDays;

            Log.d("AttendanceAdapter", "Json :"+ attendanceItem.toString());
            Log.d("AttendanceAdapter", "Subject :"+ subject);
            Log.d("AttendanceAdapter", "Attendance Percent :"+ attendancePercent);

            // Populate main list item
            holder.subjectTextView.setText(subject);
            holder.attendancePercentTextView.setText(String.format(Locale.getDefault(), "Attendance: %.2f%%", attendancePercent));
            holder.overallAttendancePercentTextView.setText(String.format(Locale.getDefault(), "%.0f%%", attendancePercent));

            Log.d("AttendanceAdapter","setText subjectview: "+ holder.subjectTextView.getText().toString());
            Log.d("AttendanceAdapter","setText percentview: "+ holder.attendancePercentTextView.getText().toString());

            // Populate expanded view
            holder.expandedSubjectTitle.setText(subject + " - Attendance");

            // Populate PieChart
            ArrayList<PieEntry> entries = new ArrayList<>();
            entries.add(new PieEntry(presentDays, "Present"));
            entries.add(new PieEntry(absentDays, "Absent"));

            PieDataSet dataSet = new PieDataSet(entries, "");
            dataSet.setColors(Color.parseColor("#4CAF50"), Color.parseColor("#F44336"));
            dataSet.setDrawValues(false);

            PieData data = new PieData(dataSet);
            holder.expandedPieChart.setData(data);
            holder.expandedPieChart.getDescription().setEnabled(false);
            holder.expandedPieChart.getLegend().setEnabled(false);
            holder.expandedPieChart.setDrawHoleEnabled(true);
            holder.expandedPieChart.setHoleRadius(60f);
            holder.expandedPieChart.invalidate();

            // Populate text views in expanded view
            holder.presentDaysValue.setText(presentDays + " days");
            holder.absentDaysValue.setText(absentDays + " days");

            // Click listener for item click (to toggle expanded view)
            holder.itemView.setOnClickListener(v -> {
                if (holder.expandedAttendanceView.getVisibility() == View.GONE) {
                    holder.expandedAttendanceView.setVisibility(View.VISIBLE);
                } else {
                    holder.expandedAttendanceView.setVisibility(View.GONE);
                }
            });

        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return attendanceSummary.length();
    }

    static class AttendanceViewHolder extends RecyclerView.ViewHolder {
        TextView subjectTextView, attendancePercentTextView, overallAttendancePercentTextView;
        View expandedAttendanceView;
        TextView expandedSubjectTitle, presentDaysValue, absentDaysValue;
        PieChart expandedPieChart;

        public AttendanceViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            attendancePercentTextView = itemView.findViewById(R.id.attendancePercentTextView);
            overallAttendancePercentTextView = itemView.findViewById(R.id.overallAttendancePercentTextView);
            expandedAttendanceView = itemView.findViewById(R.id.expandedAttendanceView);
            expandedSubjectTitle = itemView.findViewById(R.id.expandedSubjectTitle);
            presentDaysValue = itemView.findViewById(R.id.presentDaysValue);
            absentDaysValue = itemView.findViewById(R.id.absentDaysValue);
            expandedPieChart = itemView.findViewById(R.id.expandedPieChart);
        }
    }
}