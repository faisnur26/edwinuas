package com.project.edwinuas_nasmoco.api.ui.home;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import com.bumptech.glide.Glide;
import com.denzcoskun.imageslider.ImageSlider;
import com.denzcoskun.imageslider.constants.ScaleTypes;
import com.denzcoskun.imageslider.models.SlideModel;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.RegisterAPI;
import com.project.edwinuas_nasmoco.api.ServerAPI;
import com.project.edwinuas_nasmoco.api.ui.dashboard.OrderHelper;
import com.project.edwinuas_nasmoco.api.ui.product.Product;
import com.project.edwinuas_nasmoco.api.ui.product.ProductAdapter;
import com.project.edwinuas_nasmoco.api.ui.product.RetrofitClient;
import com.project.edwinuas_nasmoco.databinding.FragmentHomeBinding;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private String email;
    private OrderHelper orderHelper;
    private ProductAdapter adapter;
    private List<Product> fullProductList;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        // Initialize OrderHelper
        orderHelper = new OrderHelper(requireContext());

        // Setup RecyclerView
        binding.recyclerTrending.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerTrending.setNestedScrollingEnabled(false);

        // Get email from SharedPreferences
        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");


        // Set up image slider
        setupImageSlider();

        setupCategoryClickListeners();
        fetchTrendingProducts();

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
        binding.tabung.setOnClickListener(v -> navigateToProduct("Televisi Tabung"));
        binding.led.setOnClickListener(v -> navigateToProduct("Televisi LED"));
        binding.oled.setOnClickListener(v -> navigateToProduct("Televisi OLED"));
    }

    private void navigateToProduct(String category) {
        try {
            Bundle result = new Bundle();
            result.putString("selectedCategory", category);
            getParentFragmentManager().setFragmentResult("categoryRequest", result);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_product);

            // Update bottom nav selection
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void navigateToProductWithSearch(String searchQuery) {
        try {
            Bundle result = new Bundle();
            result.putString("searchQuery", searchQuery);
            getParentFragmentManager().setFragmentResult("searchRequest", result);

            NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);
            navController.navigate(R.id.navigation_product);

            // Update bottom nav selection
            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.nav_view);
            if (bottomNav != null) {
                bottomNav.post(() -> bottomNav.setSelectedItemId(R.id.navigation_product));
            }
        } catch (Exception e) {
            Log.e(TAG, "Navigation error", e);
            Toast.makeText(getContext(), "Navigation error", Toast.LENGTH_SHORT).show();
        }
    }

    private void fetchTrendingProducts() {
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
                    Toast.makeText(getContext(), "Failed to load products", Toast.LENGTH_SHORT).show();
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
                Toast.makeText(getContext(), product.getMerk() + " added to cart", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProductViewClick(Product product, ProductAdapter.ViewHolder holder) {
                int newViewCount = product.getViewCount() + 1;
                product.setViewCount(newViewCount);
                holder.tvView.setText("Viewed " + newViewCount + "x");

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
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                // Optional success handling
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to update view count", Toast.LENGTH_SHORT).show();
            }
        });
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}