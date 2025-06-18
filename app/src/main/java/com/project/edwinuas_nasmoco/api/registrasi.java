package com.project.edwinuas_nasmoco.api;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.project.edwinuas_nasmoco.R;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class registrasi extends AppCompatActivity {
    TextView back;
    EditText etnama, etemail, etpassword;
    Button submit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_registrasi);

        etnama = findViewById(R.id.yourname);
        etemail = findViewById(R.id.email);
        etpassword = findViewById(R.id.katasandi);
        back = findViewById(R.id.back);
        submit = findViewById(R.id.submit);

        submit.setOnClickListener(view -> prosesSubmit(etemail.getText().toString().trim(),
                etnama.getText().toString().trim(),
                etpassword.getText().toString().trim()));

        back.setOnClickListener(view -> {
            Intent intent = new Intent(registrasi.this, login.class);
            startActivity(intent);
        });

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    // Fungsi validasi email yang lebih baik
    public boolean isEmailValid(String email) {
        String expression = "^[\\w\\.-]+@([\\w\\-]+\\.)+[a-zA-Z]{2,}$";
        Pattern pattern = Pattern.compile(expression);
        Matcher matcher = pattern.matcher(email);
        return matcher.matches();
    }

    private void prosesSubmit(String vemail, String vnama, String vpassword) {
        if (vemail.isEmpty() || vnama.isEmpty() || vpassword.isEmpty()) {
            showMessage("Semua kolom harus diisi!");
            return;
        }

        if (!isEmailValid(vemail)) {
            showMessage("Email Tidak Valid!");
            return;
        }

        ServerAPI urlapi = new ServerAPI();
        String URL = urlapi.BASE_URL;

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        RegisterAPI api = retrofit.create(RegisterAPI.class);

        api.register(vemail, vnama, vpassword).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String responseBody = response.body().string();
                        Log.d("API Response", responseBody); // Logging response untuk debugging

                        // Cek apakah response berbentuk JSON
                        if (responseBody.startsWith("{")) {
                            JSONObject json = new JSONObject(responseBody);
                            if ("1".equals(json.optString("status"))) {
                                if ("1".equals(json.optString("result"))) {
                                    showMessage("Register Berhasil");
                                    etemail.setText("");
                                    etnama.setText("");
                                    etpassword.setText("");
                                } else {
                                    showMessage("Simpan Gagal");
                                }
                            } else {
                                showMessage("User Sudah Ada");
                            }
                        } else {
                            showMessage("Gagal Register! Response bukan JSON.");
                        }
                    } catch (JSONException | IOException e) {
                        e.printStackTrace();
                        showMessage("Error Parsing Response: " + e.getMessage());
                    }
                } else {
                    showMessage("Gagal Register! Response tidak valid.");
                    Log.e("Register Error", "Response Code: " + response.code());
                }
            }


            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                showMessage("Koneksi gagal: " + t.getMessage());
                Log.e("Info Register", "Register Gagal: " + t.getMessage(), t);
            }
        });
    }

    private void showMessage(String message) {
        new AlertDialog.Builder(registrasi.this)
                .setMessage(message)
                .setNegativeButton("OK", null)
                .create()
                .show();
    }
}
