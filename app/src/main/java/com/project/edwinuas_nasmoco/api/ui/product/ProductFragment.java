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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.RegisterAPI;
import com.project.edwinuas_nasmoco.databinding.FragmentProductBinding;
import com.project.edwinuas_nasmoco.api.ui.dashboard.OrderHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductFragment extends Fragment {
    private FragmentProductBinding binding;
    private RecyclerView recyclerView, categoryRecyclerView;
    private ProductAdapter adapter;
    private CategoryAdapter categoryAdapter;
    private OrderHelper orderHelper;
    private List<Product> fullProductList;
    private String selectedCategory = "Semua";

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
                return true;
            }
        });

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        fetchProduct();


        // Ensure bottom nav reflects current fragment
        BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
        if (bottomNav != null) {
            bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
        }
    }


    private void fetchProduct() {
        RegisterAPI apiService = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<List<Product>> call = apiService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    fullProductList = response.body();

                    adapter = new ProductAdapter(getContext(), fullProductList, new ProductAdapter.OnProductClickListener() {
                        @Override
                        public void onProductClick(Product product) {
                            orderHelper.addToOrder(product);
                            Toast.makeText(getContext(), product.getMerk() + " berhasil ditambahkan ke keranjang", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProductViewClick(Product product, ProductAdapter.ViewHolder holder) {
                            int newViewCount = product.getViewCount() + 1;
                            product.setViewCount(newViewCount);
                            holder.tvView.setText("Dilihat " + newViewCount + "x");

                            RegisterAPI api = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
                            Call<ResponseBody> updateCall = api.updateProductView(product.getKode(), newViewCount);
                            updateCall.enqueue(new Callback<ResponseBody>() {
                                @Override
                                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }

                                @Override
                                public void onFailure(Call<ResponseBody> call, Throwable t) {
                                    Toast.makeText(getContext(), "Gagal update view count", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    });
                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(getContext(), "Gagal mengambil data produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}