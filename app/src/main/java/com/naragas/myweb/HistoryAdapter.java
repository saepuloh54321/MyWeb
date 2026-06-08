package com.naragas.myweb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private final List<HistoryItem> historyList;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(String url);
    }

    public HistoryAdapter(List<HistoryItem> historyList, OnItemClickListener listener) {
        this.historyList = historyList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_history, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        HistoryItem item = historyList.get(position);
        holder.textUrl.setText(item.getUrl());
        holder.textTime.setText(item.getFormattedTime());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(item.getUrl()));
    }

    @Override
    public int getItemCount() {
        return historyList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textUrl, textTime;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textUrl = itemView.findViewById(R.id.textHistoryUrl);
            textTime = itemView.findViewById(R.id.textHistoryTime);
        }
    }
}
