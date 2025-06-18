
package com.project.edwinuas_nasmoco.api.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.RegisterAPI;
import com.project.edwinuas_nasmoco.api.ui.dashboard.OrderHelper;
import com.project.edwinuas_nasmoco.api.ui.product.Product;
import com.project.edwinuas_nasmoco.api.ui.product.ProductAdapter;
import com.project.edwinuas_nasmoco.api.ui.product.RetrofitClient;
import com.project.edwinuas_nasmoco.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private String email;
    private OrderHelper orderHelper;
    private ProductAdapter adapter;
    private List<Product> fullProductList;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        orderHelper = new OrderHelper(requireContext());

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        setupImageSlider();
        setupCategoryClickListeners();
        setupTrendingProducts();

        return root;
    }

    private void setupImageSlider() {
        ImageSlider imageSlider = binding.imageSlider;
        ArrayList<SlideModel> slideModels = new ArrayList<>();
        slideModels.add(new SlideModel(R.drawable.img_3, ScaleTypes.FIT));
        slideModels.add(new SlideModel(R.drawable.img_2, ScaleTypes.FIT));
        imageSlider.setImageList(slideModels, ScaleTypes.FIT);
    }

    private void setupCategoryClickListeners() {
        binding.carMpv.setOnClickListener(v -> navigateToProduct("MPV"));
        binding.carHybrid.setOnClickListener(v -> navigateToProduct("Hybrid"));
        binding.carSuv.setOnClickListener(v -> navigateToProduct("SUV"));
        binding.carSedan.setOnClickListener(v -> navigateToProduct("Sedan"));
        binding.carCommercial.setOnClickListener(v -> navigateToProduct("Commercial"));
        binding.carHatchback.setOnClickListener(v -> navigateToProduct("Hatchback"));
    }

    private void navigateToProduct(String category) {
        try {
            Bundle result = new Bundle();
            result.putString("selectedCategory", category);
            getParentFragmentManager().setFragmentResult("categoryRequest", result);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_product);

            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupTrendingProducts() {
        binding.recyclerTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerTrending.setNestedScrollingEnabled(false);

        RegisterAPI apiService = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<List<Product>> call = apiService.getProducts();

        call.enqueue(new Callback<List<Product>>() {
            @Override
            public void onResponse(Call<List<Product>> call, Response<List<Product>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = response.body();
                    allProducts.sort((p1, p2) -> Integer.compare(p2.getViewCount(), p1.getViewCount()));
                    fullProductList = allProducts.subList(0, Math.min(6, allProducts.size()));
                    setupProductAdapter();
                } else {
                    Toast.makeText(getContext(), "Gagal memuat produk", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Product>> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupProductAdapter() {
        adapter = new ProductAdapter(getContext(), fullProductList, new ProductAdapter.OnProductClickListener() {
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
                updateProductViewCount(product.getKode(), newViewCount);
            }
        });

        binding.recyclerTrending.setAdapter(adapter);
    }

    private void updateProductViewCount(String productCode, int viewCount) {
        RegisterAPI api = RetrofitClient.getRetrofitInstance().create(RegisterAPI.class);
        Call<ResponseBody> updateCall = api.updateProductView(productCode, viewCount);
        updateCall.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) { }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Gagal update view count", Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
