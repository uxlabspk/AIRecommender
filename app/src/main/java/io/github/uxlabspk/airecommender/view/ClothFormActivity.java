package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

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
    private static final String API_KEY = "Bearer ";

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
        // Check if all fields are filled
        if (!validateFormInputs()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get values from all dropdowns
        String age = binding.ageDropdown.getText().toString();
        String gender = binding.genderDropdown.getText().toString();
        String budget = binding.budgetDropdown.getText().toString();
        String bodyType = binding.bodyTypeDropdown.getText().toString();
        String fitType = binding.fitTypeDropdown.getText().toString();
        String height = binding.heightDropdown.getText().toString();
        String weight = binding.weightDropdown.getText().toString();
        String size = binding.sizeDropdown.getText().toString();
        String style = binding.styleDropdown.getText().toString();
        String colors = binding.colorsDropdown.getText().toString();
        String fabric = binding.fabricDropdown.getText().toString();
        String occasionType = binding.occasionTypeDropdown.getText().toString();
        String items = binding.itemsDropdown.getText().toString();
        String preference = binding.preferenceDropdown.getText().toString();

        // Build the prompt using all dropdown values
        String prompt = buildPrompt(age, gender, budget, bodyType, fitType, height,
                weight, size, style, colors, fabric, occasionType, items, preference);

        // Show loading indicator
        binding.recommendButton.setEnabled(false);
        binding.recommendButton.setText("Generating recommendation...");

        // Create JSON request body
        String jsonRequest = "{ \"inputs\": \"" + prompt + "\" }";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonRequest);

        // Make API call
        HuggingFaceApi apiService = RetrofitClient.getApiService();
        apiService.generateImage(API_KEY, requestBody).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                binding.recommendButton.setEnabled(true);
                binding.recommendButton.setText("Recommend");

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
                        intent.putExtra("prompt", prompt);  // Pass the prompt to result activity
                        startActivity(intent);

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error processing image: " + e.getMessage());
                        Toast.makeText(ClothFormActivity.this, "Error processing image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("API_ERROR", "Response error: " + response.errorBody());
                    Toast.makeText(ClothFormActivity.this, "Failed to generate recommendation", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                binding.recommendButton.setEnabled(true);
                binding.recommendButton.setText("Recommend");
                Log.e("API_ERROR", "Request failed: " + t.getMessage());
                Toast.makeText(ClothFormActivity.this, "Network error. Please try again", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private boolean validateFormInputs() {
        // Check if all dropdown fields have a selection
        return !binding.ageDropdown.getText().toString().isEmpty() &&
                !binding.genderDropdown.getText().toString().isEmpty() &&
                !binding.budgetDropdown.getText().toString().isEmpty() &&
                !binding.bodyTypeDropdown.getText().toString().isEmpty() &&
                !binding.fitTypeDropdown.getText().toString().isEmpty() &&
                !binding.heightDropdown.getText().toString().isEmpty() &&
                !binding.weightDropdown.getText().toString().isEmpty() &&
                !binding.sizeDropdown.getText().toString().isEmpty() &&
                !binding.styleDropdown.getText().toString().isEmpty() &&
                !binding.colorsDropdown.getText().toString().isEmpty() &&
                !binding.fabricDropdown.getText().toString().isEmpty() &&
                !binding.occasionTypeDropdown.getText().toString().isEmpty() &&
                !binding.itemsDropdown.getText().toString().isEmpty() &&
                !binding.preferenceDropdown.getText().toString().isEmpty();
    }

    private String buildPrompt(String age, String gender, String budget, String bodyType,
                               String fitType, String height, String weight, String size,
                               String style, String colors, String fabric, String occasionType,
                               String items, String preference) {
        return "Generate a realistic outfit recommendation for a " + gender + " aged " + age +
                " with a " + bodyType + " body type, " + height + " tall, weighing " + weight +
                ", wearing size " + size + ". The outfit should be " + style + " style with " +
                colors + " colors, made of " + fabric + " fabric, suitable for " + occasionType +
                " occasions. Focus on " + items + " items that fit " + fitType + ". The person prefers " +
                preference + " clothing within a budget of " + budget + ". The image should show a complete outfit on a person.";
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