package com.budgetmaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private Context context;
    private List<TransactionItem> transactionList;

    public TransactionAdapter(Context context, List<TransactionItem> transactionList) {
        this.context = context;
        this.transactionList = transactionList;
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionItem transaction = transactionList.get(position);

        // Set title (label)
        holder.tvTransactionTitle.setText(transaction.getTitle());

        // Set category
        holder.tvTransactionCategory.setText(transaction.getCategory());

        // Set date
        holder.tvTransactionDate.setText(transaction.getDate());

        // Set amount with + or - prefix and color
        if (transaction.isIncome()) {
            holder.tvTransactionAmount.setText(String.format("+%.2f DT", transaction.getAmount()));
            holder.tvTransactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.ivTransactionIcon.setImageResource(android.R.drawable.arrow_down_float);
            holder.layoutIcon.setBackgroundResource(R.drawable.circle_background_income);
        } else {
            holder.tvTransactionAmount.setText(String.format("-%.2f DT", transaction.getAmount()));
            holder.tvTransactionAmount.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.ivTransactionIcon.setImageResource(android.R.drawable.arrow_up_float);
            holder.layoutIcon.setBackgroundResource(R.drawable.circle_background_outcome);
        }
    }

    @Override
    public int getItemCount() {
        return transactionList.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        TextView tvTransactionTitle, tvTransactionCategory, tvTransactionDate, tvTransactionAmount;
        ImageView ivTransactionIcon;
        LinearLayout layoutIcon;

        public TransactionViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTransactionTitle = itemView.findViewById(R.id.tvTransactionTitle);
            tvTransactionCategory = itemView.findViewById(R.id.tvTransactionCategory);
            tvTransactionDate = itemView.findViewById(R.id.tvTransactionDate);
            tvTransactionAmount = itemView.findViewById(R.id.tvTransactionAmount);
            ivTransactionIcon = itemView.findViewById(R.id.ivTransactionIcon);
            layoutIcon = itemView.findViewById(R.id.layoutIcon);
        }
    }
}