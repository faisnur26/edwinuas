package com.project.edwinuas_nasmoco.api.ui.notifications;

import static android.app.Activity.RESULT_OK;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.loader.content.CursorLoader;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.project.edwinuas_nasmoco.R;
import com.project.edwinuas_nasmoco.api.RegisterAPI;
import com.project.edwinuas_nasmoco.api.ServerAPI;
import com.project.edwinuas_nasmoco.databinding.FragmentProfileBinding;

import org.json.JSONObject;

import java.io.File;

import id.zelory.compressor.Compressor;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class Profile extends Fragment {

    private FragmentProfileBinding binding;
    private String email;
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;

    public Profile() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        SharedPreferences sharedPreferences = requireContext().getSharedPreferences("user_session", getContext().MODE_PRIVATE);
        email = sharedPreferences.getString("email", "");

        if (!email.isEmpty()) {
            getProfile(email);
        }

        binding.btnSimpan.setOnClickListener(view -> {
            String nama = binding.etNama.getText().toString().trim();
            String alamat = binding.etAlamat.getText().toString().trim();
            String kota = binding.etKota.getText().toString().trim();
            String provinsi = binding.etProvinsi.getText().toString().trim();
            String telp = binding.etTelepon.getText().toString().trim();
            String kodepos = binding.etKodePos.getText().toString().trim();

            if (nama.isEmpty() || alamat.isEmpty() || kota.isEmpty() || provinsi.isEmpty() || telp.isEmpty() || kodepos.isEmpty()) {
                Toast.makeText(getContext(), "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                return;
            }

            DataPelanggan data = new DataPelanggan();
            data.setNama(nama);
            data.setAlamat(alamat);
            data.setKota(kota);
            data.setProvinsi(provinsi);
            data.setTelp(telp);
            data.setKodepos(kodepos);
            data.setEmail(email);

            updateProfile(data);
        });

        NavController navController = Navigation.findNavController(requireActivity(), R.id.nav_host_fragment_activity_main);

        binding.btnKembali.setOnClickListener(view -> {
            navController.navigate(R.id.navigation_notifications);
        });

        binding.btnEditPhoto.setOnClickListener(view -> {
            openImageChooser();
        });

        return root;
    }

    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            Glide.with(requireContext())
                    .load(selectedImageUri)
                    .circleCrop()
                    .into(binding.imgProfile);

            uploadImageToServer();
        }
    }

    private void uploadImageToServer() {
        if (selectedImageUri == null) return;

        String filePath = getRealPathFromURI(selectedImageUri);
        if (filePath == null) {
            Toast.makeText(getContext(), "Gagal mendapatkan path gambar", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            File originalFile = new File(filePath);

            // Gunakan Compressor 2.1.1 untuk mengompres gambar
            File compressedFile = new Compressor(requireContext())
                    .setMaxWidth(640)
                    .setMaxHeight(480)
                    .setQuality(75)
                    .compressToFile(originalFile);

            RequestBody requestFile = RequestBody.create(MediaType.parse("image/*"), compressedFile);
            MultipartBody.Part body = MultipartBody.Part.createFormData("imageupload", compressedFile.getName(), requestFile);

            RequestBody emailBody = RequestBody.create(MediaType.parse("text/plain"), email);

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(new ServerAPI().BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();

            RegisterAPI api = retrofit.create(RegisterAPI.class);
            Call<ResponseBody> call = api.uploadFotoProfil(emailBody, body);

            call.enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    try {
                        if (response.isSuccessful() && response.body() != null) {
                            JSONObject json = new JSONObject(response.body().string());
                            if (json.getInt("kode") == 1) {
                                Toast.makeText(getContext(), "Foto profil berhasil diubah", Toast.LENGTH_SHORT).show();
                                getProfile(email);
                            } else {
                                Toast.makeText(getContext(), "Gagal: " + json.getString("pesan"), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Gagal upload foto", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("UploadPhoto", "Parsing error", e);
                        Toast.makeText(getContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    Log.e("UploadPhoto", "Failed", t);
                    Toast.makeText(getContext(), "Gagal terhubung ke server", Toast.LENGTH_SHORT).show();
                }
            });

        } catch (Exception e) {
            Log.e("UploadImage", "Compress error", e);
            Toast.makeText(getContext(), "Gagal mengompres gambar", Toast.LENGTH_SHORT).show();
        }
    }

    private String getRealPathFromURI(Uri contentUri) {
        String[] proj = {MediaStore.Images.Media.DATA};
        CursorLoader loader = new CursorLoader(getContext(), contentUri, proj, null, null, null);
        Cursor cursor = loader.loadInBackground();
        int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        String result = cursor.getString(column_index);
        cursor.close();
        return result;
    }

    private void getProfile(String vemail) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.getProfile(vemail).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        JSONObject json = new JSONObject(response.body().string());
                        if (json.getString("result").equals("1")) {
                            JSONObject data = json.getJSONObject("data");
                            binding.etNama.setText(data.getString("nama"));
                            binding.etAlamat.setText(data.getString("alamat"));
                            binding.etKota.setText(data.getString("kota"));
                            binding.etProvinsi.setText(data.getString("provinsi"));
                            binding.etTelepon.setText(data.getString("telp"));
                            binding.etKodePos.setText(data.getString("kodepos"));
                            binding.etEmail.setText(email);
                            binding.etEmail.setEnabled(false);

                            String fotoFilename = data.optString("filename", "");
                            if (!fotoFilename.isEmpty()) {
                                String imageUrl = new ServerAPI().IMAGE_BASE_URL + fotoFilename;

                                Glide.with(requireContext())
                                        .load(imageUrl)
                                        .circleCrop()
                                        .placeholder(R.drawable.logo)
                                        .error(R.drawable.logo)
                                        .into(binding.imgProfile);
                            }

                        } else {
                            Toast.makeText(getContext(), "Data tidak ditemukan", Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Log.e("GetProfile", "Error parsing", e);
                        Toast.makeText(getContext(), "Terjadi kesalahan", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(getContext(), "Gagal mengambil data", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("GetProfile", "onFailure", t);
                Toast.makeText(getContext(), "Terjadi kesalahan koneksi", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateProfile(DataPelanggan data) {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(new ServerAPI().BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);
        api.updateProfile(
                data.getNama(),
                data.getAlamat(),
                data.getKota(),
                data.getProvinsi(),
                data.getTelp(),
                data.getKodepos(),
                data.getEmail()
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        String responseString = response.body().string();
                        Log.e("UpdateProfile", "Raw response: " + responseString);
                        try {
                            JSONObject json = new JSONObject(responseString);
                            if (json.has("message")) {
                                Toast.makeText(getContext(), json.getString("message"), Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Berhasil, tapi tidak ada pesan", Toast.LENGTH_SHORT).show();
                            }
                        } catch (Exception e) {
                            Log.e("UpdateProfile", "JSON parse error", e);
                            Toast.makeText(getContext(), "Response bukan JSON: " + responseString, Toast.LENGTH_LONG).show();
                        }

                    } else {
                        Toast.makeText(getContext(), "Gagal menyimpan perubahan", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    Log.e("UpdateProfile", "Exception saat membaca response", e);
                    Toast.makeText(getContext(), "Terjadi kesalahan saat membaca respon", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("UpdateProfile", "Koneksi gagal", t);
                new AlertDialog.Builder(getContext())
                        .setMessage("Simpan Gagal: " + t.getMessage())
                        .setNegativeButton("Retry", null)
                        .create().show();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
