package com.example.attendanceapp;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

// Ensure this matches your package structure
import com.example.attendanceapp.R;
import java.util.ArrayList;

public class StudentAdapter extends RecyclerView.Adapter<StudentAdapter.StudentViewHolder> {

    ArrayList<Student> studentItems;
    Context context;

    private OnItemClickListener onItemClickListener;
    private OnItemLongClickListener onItemLongClickListener;

    public interface OnItemClickListener {
        void onClick(int position);
    }

    public interface OnItemLongClickListener {
        void onLongClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    public void setOnItemLongClickListener(OnItemLongClickListener onItemLongClickListener) {
        this.onItemLongClickListener = onItemLongClickListener;
    }

    public StudentAdapter(Context context, ArrayList<Student> studentItems) {
        this.studentItems = studentItems;
        this.context = context;
    }

    public static class StudentViewHolder extends RecyclerView.ViewHolder {
        TextView roll;
        TextView name;
        TextView status;
        CardView cardView;

        public StudentViewHolder(@NonNull View itemView, OnItemClickListener listener, OnItemLongClickListener longListener) {
            super(itemView);
            // MATCHING YOUR XML IDs EXACTLY:
            roll = itemView.findViewById(R.id.tv_roll_number);
            name = itemView.findViewById(R.id.tv_student_name);
            status = itemView.findViewById(R.id.status);      
            cardView = itemView.findViewById(R.id.cardView); 

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onClick(getAdapterPosition());
                }
            });

            itemView.setOnLongClickListener(v -> {
                if (longListener != null) {
                    longListener.onLongClick(getAdapterPosition());
                    return true;
                }
                return false;
            });
        }
    }

    @NonNull
    @Override
    public StudentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.student_item, parent, false);
        return new StudentViewHolder(view, onItemClickListener, onItemLongClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull StudentViewHolder holder, int position) {
        holder.roll.setText(studentItems.get(position).getRollNumber());
        holder.name.setText(studentItems.get(position).getName());
        holder.status.setText(studentItems.get(position).getStatus());

        if (studentItems.get(position).getStatus().equals("P")) {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#ffffff")); // White
            holder.status.setTextColor(Color.parseColor("#4CAF50")); // Green
        } else {
            holder.cardView.setCardBackgroundColor(Color.parseColor("#ffcdd2")); // Red
            holder.status.setTextColor(Color.RED);
        }
    }

    @Override
    public int getItemCount() {
        return studentItems.size();
    }
}
