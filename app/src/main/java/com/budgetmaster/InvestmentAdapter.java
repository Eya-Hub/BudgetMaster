package com.budgetmaster;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class InvestmentAdapter extends RecyclerView.Adapter<InvestmentAdapter.InvestmentViewHolder> {

    private Context context;
    private List<Investment> investmentList;

    public InvestmentAdapter(Context context, List<Investment> investmentList) {
        this.context = context;
        this.investmentList = investmentList;
    }

    @NonNull
    @Override
    public InvestmentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_investment, parent, false);
        return new InvestmentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InvestmentViewHolder holder, int position) {
        Investment investment = investmentList.get(position);

        // Set label
        holder.tvLabel.setText(investment.getLabel());

        // Set amount
        holder.tvAmount.setText(String.format("%.2f DT", investment.getAmount()));

        // Set date if available
        if (investment.getDate() != null && !investment.getDate().isEmpty()) {
            holder.tvDate.setVisibility(View.VISIBLE);
            holder.tvDate.setText(investment.getDate());
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return investmentList.size();
    }

    public static class InvestmentViewHolder extends RecyclerView.ViewHolder {
        TextView tvLabel, tvDate, tvAmount;

        public InvestmentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvLabel = itemView.findViewById(R.id.item_label);
            tvDate = itemView.findViewById(R.id.item_date);
            tvAmount = itemView.findViewById(R.id.item_amount);
        }
    }
}