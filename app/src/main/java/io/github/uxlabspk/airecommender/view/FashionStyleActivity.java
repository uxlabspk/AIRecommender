package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.uxlabspk.airecommender.BuildConfig;
import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.api.HuggingFaceApi;
import io.github.uxlabspk.airecommender.api.RetrofitClient;
import io.github.uxlabspk.airecommender.databinding.ActivityFashionStyleBinding;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FashionStyleActivity extends AppCompatActivity {
    private ActivityFashionStyleBinding binding;
    private static final String API_KEY = "Bearer " + BuildConfig.apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFashionStyleBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back
        binding.backButton.setOnClickListener(v -> finish());
        // Recommend
        binding.recommendButton.setOnClickListener(v -> generateCompleteLook());

        // dropdowns
        setupPersonalInfoDropdowns();
        setupBodyMeasurementsDropdowns();
        setupSkinDetailsDropdowns();
        setupStyleDropdowns();
        setupOccasionDropdown();
        setupPreferencesDropdowns();
    }

    private void setupPersonalInfoDropdowns() {
        binding.ageDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"18-24", "25-34", "35-44", "45-54", "55+"}));

        binding.genderDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Male", "Female", "Non-binary", "Prefer not to say"}));

        binding.budgetDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"PKR 0-15,000",
                        "PKR 15,000-30,000",
                        "PKR 30,000-60,000",
                        "PKR 60,000-150,000",
                        "PKR 150,000+"}));
    }

    private void setupBodyMeasurementsDropdowns() {
        binding.bodyTypeDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Slim", "Athletic", "Average", "Curvy", "Plus Size"}));

        binding.fitTypeDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Slim Fit", "Regular Fit", "Relaxed Fit", "Oversize"}));

        binding.heightDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"<5'0\"", "5'0\"-5'4\"", "5'5\"-5'8\"", "5'9\"-6'0\"", ">6'0\""}));

        binding.weightDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"<120 lbs", "120-150 lbs", "151-180 lbs", "181-210 lbs", ">210 lbs"}));

        binding.sizeDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"XS", "S", "M", "L", "XL", "XXL"}));
    }

    private void setupSkinDetailsDropdowns() {
        binding.skinToneDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Fair", "Light", "Medium", "Olive", "Tan", "Deep"}));

        binding.skinTypeDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Dry", "Oily", "Combination", "Sensitive", "Normal"}));
    }

    private void setupStyleDropdowns() {
        binding.clothingStyleDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Casual", "Formal", "Business", "Sporty", "Bohemian", "Vintage"}));

        binding.makeupStyleDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Natural", "Glam", "Bold", "Smokey", "Minimal"}));

        binding.colorsDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Neutrals", "Bright", "Pastels", "Earth Tones", "Monochrome"}));

        binding.fabricDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Cotton", "Linen", "Silk", "Wool", "Synthetic", "Denim"}));

        binding.makeupProductsDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Lipsticks", "Foundations", "Eyeshadows", "Blushes", "Mascaras"}));
    }

    private void setupOccasionDropdown() {
        binding.occasionTypeDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Everyday", "Work", "Date Night", "Party", "Wedding", "Vacation"}));
    }

    private void setupPreferencesDropdowns() {
        binding.itemsDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Shirts", "Pants", "Dresses", "Shalwar Qameez", "Skirts", "Jeans", "Jackets"}));

        binding.preferenceDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Comfort", "Trendy", "Professional", "Edgy", "Minimal"}));

        binding.shadesDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Matte", "Shimmer", "Glowy", "Natural", "Bold"}));

        binding.accessoriesDropdown.setAdapter(new ArrayAdapter<>(this,
                R.layout.dropdown_item,
                new String[]{"Necklace", "Earrings", "Bracelet", "Watch", "Hat", "Belt"}));
    }

    private void generateCompleteLook() {
        // show “Thinking…” dialog
        ProgressStatus progressStatus = new ProgressStatus(this);
        progressStatus.setCanceledOnTouchOutside(false);
        progressStatus.setTitle("Thinking...");
        progressStatus.show();

        // disable button
        binding.recommendButton.setEnabled(false);
        binding.recommendButton.setText(R.string.generating_recommendation);

        // collect inputs
        String prompt = String.format(
                "Generate a culturally authentic Pakistani‑Islamic outfit and makeup recommendation for a %s, age %s, budget %s, body type %s, fit %s, height %s, weight %s, size %s, skin tone %s, skin type %s, clothing style %s, makeup style %s, colors %s, fabric %s, makeup products %s, occasion %s, items %s, preference %s, shades %s, accessories %s. " +
                        "Focus on modest Islamic aesthetics and traditional Pakistani textiles and silhouettes—think embroidered shalwar kameez, dupatta drapes, hijab options, native block prints and silk fabrics—and recommend halal‑friendly makeup that complements the look.",
                binding.genderDropdown.getText(),
                binding.ageDropdown.getText(),
                binding.budgetDropdown.getText(),
                binding.bodyTypeDropdown.getText(),
                binding.fitTypeDropdown.getText(),
                binding.heightDropdown.getText(),
                binding.weightDropdown.getText(),
                binding.sizeDropdown.getText(),
                binding.skinToneDropdown.getText(),
                binding.skinTypeDropdown.getText(),
                binding.clothingStyleDropdown.getText(),
                binding.makeupStyleDropdown.getText(),
                binding.colorsDropdown.getText(),
                binding.fabricDropdown.getText(),
                binding.makeupProductsDropdown.getText(),
                binding.occasionTypeDropdown.getText(),
                binding.itemsDropdown.getText(),
                binding.preferenceDropdown.getText(),
                binding.shadesDropdown.getText(),
                binding.accessoriesDropdown.getText()
        );

        // JSON body
        Gson gson = new Gson();
        Map<String,String> payload = new HashMap<>();
        payload.put("inputs", prompt);
        String json = gson.toJson(payload);

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                json
        );

        // enqueue with retry logic
        HuggingFaceApi api = RetrofitClient.getApiService();
        api.generateImage(API_KEY, body).enqueue(new Callback<ResponseBody>() {
            private static final int MAX_RETRIES = 2;
            private int retryCount = 0;

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call,
                                   @NonNull Response<ResponseBody> resp) {
                progressStatus.dismiss();
                binding.recommendButton.setEnabled(true);
                binding.recommendButton.setText(R.string.recommend);

                if (resp.isSuccessful() && resp.body() != null) {
                    try (InputStream in = resp.body().byteStream()) {
                        File outFile = new File(
                                getExternalFilesDir(null),
                                "look_" + UUID.randomUUID() + ".png"
                        );
                        OutputStream out = android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O
                                ? Files.newOutputStream(outFile.toPath())
                                : new FileOutputStream(outFile);

                        byte[] buf = new byte[4096];
                        int r;
                        while ((r = in.read(buf)) > 0) out.write(buf, 0, r);
                        out.close();

                        Intent i = new Intent(FashionStyleActivity.this, ResultActivity.class);
                        i.putExtra("image_path", outFile.getAbsolutePath());
                        i.putExtra("prompt", prompt);
                        startActivity(i);
                    } catch (Exception e) {
                        Log.e("FashionStyle", "Save failed", e);
                        Toast.makeText(FashionStyleActivity.this,
                                "Save failed: " + e.getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                } else {
                    int code = resp.code();
                    Log.e("FashionStyle", "API error " + code);
                    if (code == 429 || code == 503 || code == 504) {
                        retryRequest(call);
                    } else {
                        Toast.makeText(FashionStyleActivity.this,
                                "Failed (Error " + code + ")",
                                Toast.LENGTH_LONG).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call,
                                  @NonNull Throwable t) {
                progressStatus.dismiss();
                binding.recommendButton.setEnabled(true);
                binding.recommendButton.setText(R.string.recommend);
                Log.e("FashionStyle", "Network error", t);
                if (t instanceof java.io.InterruptedIOException) {
                    retryRequest(call);
                } else {
                    Toast.makeText(FashionStyleActivity.this,
                            "Network error. Please try again.",
                            Toast.LENGTH_LONG).show();
                }
            }

            private void retryRequest(Call<ResponseBody> call) {
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    long backoff = 1000L * (long)Math.pow(2, retryCount);
                    Toast.makeText(FashionStyleActivity.this,
                            "Server busy, retrying... (" + retryCount + "/" + MAX_RETRIES + ")",
                            Toast.LENGTH_SHORT).show();
                    new Handler().postDelayed(() -> call.clone().enqueue(this), backoff);
                } else {
                    Toast.makeText(FashionStyleActivity.this,
                            "Server is busy. Try again later.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
