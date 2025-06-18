package com.project.edwinuas_nasmoco.api.ui.home;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.ServerAPI;
import com.project.edwinuas_nasmoco.api.model.Mobil;
import com.project.edwinuas_nasmoco.api.ui.dashboard.OrderHelper;
import com.project.edwinuas_nasmoco.api.ui.product.Product;

import java.util.List;

public class MobilAdapter extends RecyclerView.Adapter<MobilAdapter.ViewHolder> {

    private final Context context;
    private List<Mobil> mobilList;
    private final OrderHelper orderHelper;

    public MobilAdapter(Context context, List<Mobil> mobilList, OrderHelper orderHelper) {
        this.context = context;
        this.mobilList = mobilList;
        this.orderHelper = orderHelper;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.list_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Mobil mobil = mobilList.get(position);

        holder.tvMerk.setText(mobil.getMerk());
        holder.tvHarga.setText("Rp " + String.format("%,d", mobil.getHargajual()));

        Glide.with(context)
                .load(new ServerAPI().IMAGE_BASE_URL + mobil.getFoto())
                .placeholder(R.drawable.ic_car_sedan)
                .error(R.drawable.ic_car_sedan)
                .into(holder.ivFoto);

        holder.itemView.setOnClickListener(v -> {
            Product product = new Product(
                    mobil.getKode(),
                    mobil.getMerk(),
                    mobil.getHargajual(),
                    1,
                    "Tersedia",
                    mobil.getFoto(),
                    mobil.getKategori(),
                    "-",
                    0
            );

            orderHelper.addToOrder(product);
            Toast.makeText(context, mobil.getMerk() + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return mobilList != null ? mobilList.size() : 0;
    }

    public void updateData(List<Mobil> newList) {
        mobilList = newList;
        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvMerk, tvHarga;
        ImageView ivFoto;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMerk = itemView.findViewById(R.id.tvMerk);
            tvHarga = itemView.findViewById(R.id.tvHarga);
            ivFoto = itemView.findViewById(R.id.imageProduct);
        }
    }
}
