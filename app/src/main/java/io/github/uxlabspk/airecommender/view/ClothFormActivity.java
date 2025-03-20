package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;

import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.api.HuggingFaceApi;
import io.github.uxlabspk.airecommender.api.RetrofitClient;
import io.github.uxlabspk.airecommender.databinding.ActivityClothFormBinding;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ClothFormActivity extends AppCompatActivity {
    private ActivityClothFormBinding binding;
    private static final String API_KEY = "Bearer hf_key";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClothFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // on back button clicked
        binding.backButton.setOnClickListener(v-> finish());

        // on recommend button clicked
        binding.recommendButton.setOnClickListener(v -> recommend());

        // Setup all dropdown menus
        setupAgeDropdown();
        setupGenderDropdown();
        setupBudgetDropdown();
        setupBodyTypeDropdown();
        setupFitTypeDropdown();
        setupHeightDropdown();
        setupWeightDropdown();
        setupSizeDropdown();
        setupStyleDropdown();
        setupColorsDropdown();
        setupFabricDropdown();
        setupOccasionTypeDropdown();
        setupItemsDropdown();
        setupPreferenceDropdown();
    }

    private void recommend() {
        // Create JSON request body
        String jsonRequest = "{ \"inputs\": \"Astronaut riding a horse\" }";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonRequest);

        // Make API call
        HuggingFaceApi apiService = RetrofitClient.getApiService();
        apiService.generateImage(API_KEY, requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // Convert ResponseBody to an Image
                    try {
                        InputStream inputStream = response.body().byteStream();
                        File file = new File(getCacheDir(), "generated_image.png");
                        OutputStream outputStream = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            outputStream = Files.newOutputStream(file.toPath());
                        }

                        byte[] buffer = new byte[1024];
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            assert outputStream != null;
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        assert outputStream != null;
                        outputStream.close();
                        inputStream.close();

                        Intent intent = new Intent(ClothFormActivity.this, ResultActivity.class);
                        intent.putExtra("image_path", file.getAbsolutePath());
                        startActivity(intent);

                        // Load image into ImageView
//                        runOnUiThread(() -> Glide.with(ClothFormActivity.this)
//                                .load(file)
//                                .into(binding.previewImage));
//                        Toast.makeText(ClothFormActivity.this, "Generating recommendations...", Toast.LENGTH_SHORT).show();
                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error processing image: " + e.getMessage());
                    }
                } else {
                    Log.e("API_ERROR", "Response error: " + response.errorBody());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e("API_ERROR", "Request failed: " + t.getMessage());
            }
        });
    }

    private void setupAgeDropdown() {
        String[] items = new String[]{"18-24", "25-34", "35-44", "45-54", "55+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.ageDropdown.setAdapter(adapter);
    }

    private void setupGenderDropdown() {
        String[] items = new String[]{"Male", "Female", "Non-binary", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.genderDropdown.setAdapter(adapter);
    }

    private void setupBudgetDropdown() {
        String[] items = new String[]{"$0-$50", "$50-$100", "$100-$200", "$200-$500", "$500+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.budgetDropdown.setAdapter(adapter);
    }

    private void setupBodyTypeDropdown() {
        String[] items = new String[]{"Slim", "Athletic", "Average", "Curvy", "Plus Size"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.bodyTypeDropdown.setAdapter(adapter);
    }

    private void setupFitTypeDropdown() {
        String[] items = new String[]{"Slim Fit", "Regular Fit", "Relaxed Fit", "Oversized"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.fitTypeDropdown.setAdapter(adapter);
    }

    private void setupHeightDropdown() {
        String[] items = new String[]{"<5'0\"", "5'0\"-5'4\"", "5'5\"-5'8\"", "5'9\"-6'0\"", ">6'0\""};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.heightDropdown.setAdapter(adapter);
    }

    private void setupWeightDropdown() {
        String[] items = new String[]{"<120 lbs", "120-150 lbs", "151-180 lbs", "181-210 lbs", ">210 lbs"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.weightDropdown.setAdapter(adapter);
    }

    private void setupSizeDropdown() {
        String[] items = new String[]{"XS", "S", "M", "L", "XL", "XXL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.sizeDropdown.setAdapter(adapter);
    }

    private void setupStyleDropdown() {
        String[] items = new String[]{"Casual", "Formal", "Business", "Sporty", "Bohemian", "Vintage"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.styleDropdown.setAdapter(adapter);
    }

    private void setupColorsDropdown() {
        String[] items = new String[]{"Neutrals", "Bright", "Pastels", "Earth Tones", "Monochrome"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.colorsDropdown.setAdapter(adapter);
    }

    private void setupFabricDropdown() {
        String[] items = new String[]{"Cotton", "Linen", "Silk", "Wool", "Synthetic", "Denim"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.fabricDropdown.setAdapter(adapter);
    }

    private void setupOccasionTypeDropdown() {
        String[] items = new String[]{"Everyday", "Work", "Date Night", "Party", "Wedding", "Vacation"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.occasionTypeDropdown.setAdapter(adapter);
    }

    private void setupItemsDropdown() {
        String[] items = new String[]{"Shirts", "Pants", "Dresses", "Skirts", "Outerwear", "Accessories"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.itemsDropdown.setAdapter(adapter);
    }

    private void setupPreferenceDropdown() {
        String[] items = new String[]{"Trendy", "Classic", "Sustainable", "Budget-friendly", "Designer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        binding.preferenceDropdown.setAdapter(adapter);
    }
}