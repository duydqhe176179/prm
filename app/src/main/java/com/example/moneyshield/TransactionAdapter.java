package com.example.moneyshield;

import static com.example.moneyshield.function.FormatMoney.formatMoney;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.example.moneyshield.model.Transaction;

import java.util.List;

public class TransactionAdapter extends RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder> {

    private List<Transaction> transactions;
    private Context context;

    public TransactionAdapter(Context context, List<Transaction> transactions) {
        this.context = context;
        this.transactions = transactions;
    }

    @Override
    public TransactionViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_list_transaction, parent, false);
        return new TransactionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TransactionViewHolder holder, int position) {
        Transaction transaction = transactions.get(position);

        // Set data to views
        holder.contentTran.setText(transaction.getDescription());
        holder.dateTran.setText(transaction.getDate());
        holder.moneyTran.setText(formatMoney(transaction.getAmount())); // Assuming amount is an integer

        // Set icon and color based on transaction type
        if (transaction.getType().equals("Income")) {
            holder.iconTran.setImageResource(R.drawable.income);
            holder.moneyTran.setTextColor(context.getResources().getColor(R.color.money_in)); // #4380E6
        } else if (transaction.getType().equals("Expense")) {
            holder.iconTran.setImageResource(R.drawable.expense);
            holder.moneyTran.setTextColor(context.getResources().getColor(R.color.money_out)); // #CA2222
        }
    }


    @Override
    public int getItemCount() {
        return transactions.size();
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {

        TextView contentTran, dateTran, moneyTran;
        ImageView iconTran;

        public TransactionViewHolder(View itemView) {
            super(itemView);
            contentTran = itemView.findViewById(R.id.contentTran);
            dateTran = itemView.findViewById(R.id.dateTran);
            moneyTran = itemView.findViewById(R.id.moneyTran);
            iconTran = itemView.findViewById(R.id.iconTran);
        }
    }
}
