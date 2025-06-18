package com.project.edwinuas_nasmoco.api.ui.product;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.RegisterAPI;
import com.project.edwinuas_nasmoco.api.model.Mobil;
import com.project.edwinuas_nasmoco.api.model.MobilResponse;
import com.project.edwinuas_nasmoco.api.ui.dashboard.OrderHelper;
import com.project.edwinuas_nasmoco.databinding.FragmentProductBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductFragment extends Fragment {

    private FragmentProductBinding binding;
    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> fullProductList = new ArrayList<>();
    private OrderHelper orderHelper;
    private String selectedCategory = null;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProductBinding.inflate(inflater, container, false);
        recyclerView = binding.recyclerView;
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        orderHelper = new OrderHelper(requireContext());

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) { return false; }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProductList(newText);
                return true;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getParentFragmentManager().setFragmentResultListener("categoryRequest", getViewLifecycleOwner(),
                (requestKey, bundle) -> {
                    selectedCategory = bundle.getString("selectedCategory");
                    if (selectedCategory != null) {
                        fetchMobilByCategory(selectedCategory);
                    }
                });

        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
        if (bottomNav != null) {
            bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
        }
    }

    private void fetchMobilByCategory(String kategori) {
        RegisterAPI api = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<MobilResponse> call = api.getMobilByKategori(kategori);

        call.enqueue(new Callback<MobilResponse>() {
            @Override
            public void onResponse(Call<MobilResponse> call, Response<MobilResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<Mobil> mobilList = response.body().getData();
                    fullProductList.clear();

                    for (Mobil mobil : mobilList) {
                        Product product = new Product(
                                mobil.getKode(),
                                mobil.getMerk(),
                                mobil.getHargajual(),
                                1, // stok default
                                "Tersedia",
                                mobil.getFoto(),
                                mobil.getKategori(),
                                "-", // deskripsi default
                                0 // view count default
                        );
                        fullProductList.add(product);
                    }

                    if (productAdapter == null) {
                        productAdapter = new ProductAdapter(getContext(), fullProductList, new ProductAdapter.OnProductClickListener() {
                            @Override
                            public void onProductClick(Product product) {
                                orderHelper.addToOrder(product);
                                Toast.makeText(getContext(), product.getMerk() + " ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                            }

                            @Override
                            public void onProductViewClick(Product product, ProductAdapter.ViewHolder holder) {
                                int newViewCount = product.getViewCount() + 1;
                                product.setViewCount(newViewCount);
                                holder.tvView.setText("Dilihat " + newViewCount + "x");
                            }
                        });
                        recyclerView.setAdapter(productAdapter);
                    } else {
                        productAdapter.updateData(fullProductList);
                    }
                } else {
                    Toast.makeText(getContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<MobilResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal koneksi: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void filterProductList(String query) {
        if (productAdapter == null || fullProductList == null) return;

        List<Product> filteredList = new ArrayList<>();
        for (Product product : fullProductList) {
            if (product.getMerk().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }

        productAdapter.updateData(filteredList);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}