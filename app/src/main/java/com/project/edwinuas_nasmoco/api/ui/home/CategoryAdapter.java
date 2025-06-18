package com.project.edwinuas_nasmoco.api.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.model.Category;

import java.util.List;

public class CategoryAdapter extends RecyclerView.Adapter<CategoryAdapter.ViewHolder> {

    private final List<Category> categoryList;
    private final Context context;
    private final OnCategoryClickListener listener;

    public interface OnCategoryClickListener {
        void onCategoryClick(String categoryName);
    }

    public CategoryAdapter(Context context, List<Category> list, OnCategoryClickListener listener) {
        this.context = context;
        this.categoryList = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_home, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categoryList.get(position);
        holder.tvKategori.setText(category.getNama());
        holder.ivKategori.setImageResource(category.getIconResId());

        holder.itemView.setOnClickListener(v -> listener.onCategoryClick(category.getNama()));
    }

    @Override
    public int getItemCount() {
        return categoryList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivKategori;
        TextView tvKategori;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            ivKategori = itemView.findViewById(R.id.ivKategori);
            tvKategori = itemView.findViewById(R.id.tvKategori);
        }
    }
}
