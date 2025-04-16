package com.example.exp2; // Replace with your actual package name

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.exp2.R;

import java.util.List;

public class ClubAdapter extends RecyclerView.Adapter<ClubAdapter.ClubViewHolder> {

    private List<Club> clubList;
    private OnItemClickListener listener;

    public ClubAdapter(List<Club> clubList) {
        this.clubList = clubList;
    }

    public interface OnItemClickListener {
        void onApproveClick(int position, Club club);
        void onRejectClick(int position, Club club);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ClubViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.club_view, parent, false);
        return new ClubViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ClubViewHolder holder, int position) {
        Club currentClub = clubList.get(position);
        holder.studentNameTextView.setText(currentClub.getName());
        holder.rollNoTextView.setText("Roll No: " + currentClub.getRollNo());
        holder.subjectTextView.setText("Subject: " + currentClub.getSubject());

        // Expanded Details
        holder.dateTextView.setText("Date: " + currentClub.getDate());
        holder.reasonTextView.setText("Reason: " + currentClub.getReason());
        holder.letterUrlTextView.setText("Letter URL: " + currentClub.getLetterUrl());
        holder.letterPathTextView.setText("Letter Path: " + currentClub.getLetterPath());
        holder.statusTextView.setText("Status: " + currentClub.getStatus());

        // Set status color and button enablement
        if (currentClub.getStatus().equalsIgnoreCase("Pending")) {
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.pendingColor));
            holder.approveButton.setEnabled(true);
            holder.rejectButton.setEnabled(true);
        } else if (currentClub.getStatus().equalsIgnoreCase("Approved")) {
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.approvedColor));
            holder.approveButton.setEnabled(false);
            holder.rejectButton.setEnabled(false);
        } else if (currentClub.getStatus().equalsIgnoreCase("Rejected")) {
            holder.statusTextView.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.rejectedColor));
            holder.approveButton.setEnabled(false);
            holder.rejectButton.setEnabled(false);
        }

        holder.approveButton.setOnClickListener(v -> {
            if (listener != null && currentClub.getStatus().equalsIgnoreCase("Pending")) {
                listener.onApproveClick(position, currentClub);
            }
        });

        holder.rejectButton.setOnClickListener(v -> {
            if (listener != null && currentClub.getStatus().equalsIgnoreCase("Pending")) {
                listener.onRejectClick(position, currentClub);
            }
        });

        // Toggle visibility of expanded details on card click
        holder.requestCardView.setOnClickListener(v -> {
            holder.expandedDetailsLayout.setVisibility(holder.expandedDetailsLayout.getVisibility() == View.GONE ? View.VISIBLE : View.GONE);
        });
    }

    @Override
    public int getItemCount() {
        return clubList.size();
    }

    public static class ClubViewHolder extends RecyclerView.ViewHolder {
        public androidx.cardview.widget.CardView requestCardView;
        public TextView studentNameTextView;
        public TextView rollNoTextView;
        public TextView subjectTextView;
        public TextView dateTextView;
        public TextView reasonTextView;
        public TextView letterUrlTextView;
        public TextView letterPathTextView;
        public TextView statusTextView;
        public Button approveButton;
        public Button rejectButton;
        public LinearLayout expandedDetailsLayout;

        public ClubViewHolder(@NonNull View itemView) {
            super(itemView);
            requestCardView = itemView.findViewById(R.id.requestCardView);
            studentNameTextView = itemView.findViewById(R.id.studentNameTextView);
            rollNoTextView = itemView.findViewById(R.id.rollNoTextView);
            subjectTextView = itemView.findViewById(R.id.subjectTextView);
            dateTextView = itemView.findViewById(R.id.dateTextView);
            reasonTextView = itemView.findViewById(R.id.reasonTextView);
            letterUrlTextView = itemView.findViewById(R.id.letterUrlTextView);
            letterPathTextView = itemView.findViewById(R.id.letterPathTextView);
            statusTextView = itemView.findViewById(R.id.statusTextView);
            approveButton = itemView.findViewById(R.id.approveButton);
            rejectButton = itemView.findViewById(R.id.rejectButton);
            expandedDetailsLayout = itemView.findViewById(R.id.expandedDetailsLayout);
        }
    }
}