package com.budgetmaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.util.List;

public class GoalsAdapter extends RecyclerView.Adapter<GoalsAdapter.GoalViewHolder> {

    private Context context;
    private List<Goal> goalList;
    private OnGoalActionListener actionListener;

    public interface OnGoalActionListener {
        void onGoalDelete(Goal goal, int position);
        void onAddContribution(Goal goal);
    }

    public GoalsAdapter(Context context, List<Goal> goalList, OnGoalActionListener listener) {
        this.context = context;
        this.goalList = goalList;
        this.actionListener = listener;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);

        holder.tvGoalName.setText(goal.getGoalName());
        holder.tvCurrentAmount.setText(String.format("%.2f DT", goal.getCurrentAmount()));
        holder.tvTargetAmount.setText(String.format("%.2f DT", goal.getTargetAmount()));
        holder.tvProgressPercentage.setText(String.format("%d%%", goal.getProgressPercentage()));
        holder.tvRemainingAmount.setText(String.format("%.2f DT remaining", goal.getRemainingAmount()));

        // Set progress bar
        holder.progressBar.setProgress(goal.getProgressPercentage());

        // Show/hide achievement badge
        if (goal.isCompleted()) {
            holder.tvAchievementBadge.setVisibility(View.VISIBLE);
            holder.tvGoalStatus.setText("Completed! 🎉");
        } else {
            holder.tvAchievementBadge.setVisibility(View.GONE);
            holder.tvGoalStatus.setText("In Progress");
        }

        // Button listeners
        holder.btnDeleteGoal.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onGoalDelete(goal, holder.getAdapterPosition());
            }
        });

        holder.btnAddContribution.setOnClickListener(v -> {
            if (actionListener != null) {
                actionListener.onAddContribution(goal);
            }
        });
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView tvGoalName, tvGoalStatus, tvCurrentAmount, tvTargetAmount;
        TextView tvProgressPercentage, tvRemainingAmount, tvAchievementBadge;
        ProgressBar progressBar;
        MaterialButton btnDeleteGoal, btnAddContribution;

        public GoalViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGoalName = itemView.findViewById(R.id.tvGoalName);
            tvGoalStatus = itemView.findViewById(R.id.tvGoalStatus);
            tvCurrentAmount = itemView.findViewById(R.id.tvCurrentAmount);
            tvTargetAmount = itemView.findViewById(R.id.tvTargetAmount);
            tvProgressPercentage = itemView.findViewById(R.id.tvProgressPercentage);
            tvRemainingAmount = itemView.findViewById(R.id.tvRemainingAmount);
            tvAchievementBadge = itemView.findViewById(R.id.tvAchievementBadge);
            progressBar = itemView.findViewById(R.id.progressBar);
            btnDeleteGoal = itemView.findViewById(R.id.btnDeleteGoal);
            btnAddContribution = itemView.findViewById(R.id.btnAddContribution);
        }
    }
}