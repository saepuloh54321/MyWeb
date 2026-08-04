package com.naragas.myweb;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WebAdapter extends RecyclerView.Adapter<WebAdapter.ViewHolder> {

    private List<WebSite> webSitesFull;
    private List<WebSite> webSitesFiltered;
    private final OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(WebSite site);
        void onEditClick(int position, WebSite site);
        void onDeleteClick(WebSite site);
    }

    public WebAdapter(List<WebSite> webSites, OnItemClickListener listener) {
        this.webSitesFull = webSites;
        this.webSitesFiltered = new ArrayList<>(webSites);
        this.listener = listener;
    }

    public void updateList(List<WebSite> newList) {
        this.webSitesFull = newList;
        this.webSitesFiltered = new ArrayList<>(newList);
        notifyDataSetChanged();
    }

    public void filter(String query) {
        if (query.isEmpty()) {
            webSitesFiltered = new ArrayList<>(webSitesFull);
        } else {
            String lowerCaseQuery = query.toLowerCase();
            webSitesFiltered = webSitesFull.stream()
                    .filter(site -> site.getName().toLowerCase().contains(lowerCaseQuery) || 
                                   site.getUrl().toLowerCase().contains(lowerCaseQuery))
                    .collect(Collectors.toList());
        }
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_website, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WebSite site = webSitesFiltered.get(position);
        holder.textName.setText(site.getName());
        holder.textLastAccessed.setText(site.getFormattedLastAccessed());
        holder.textUrl.setText(site.getUrl());
        holder.itemView.setOnClickListener(v -> listener.onItemClick(site));
        holder.btnEdit.setOnClickListener(v -> listener.onEditClick(position, site));
        holder.btnDelete.setOnClickListener(v -> listener.onDeleteClick(site));
    }

    @Override
    public int getItemCount() {
        return webSitesFiltered.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textName, textUrl, textLastAccessed;
        ImageButton btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textName = itemView.findViewById(R.id.textName);
            textLastAccessed = itemView.findViewById(R.id.textLastAccessed);
            textUrl = itemView.findViewById(R.id.textUrl);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
        }
    }
}
