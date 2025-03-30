package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.UUID;

import io.github.uxlabspk.airecommender.BuildConfig;
import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.api.HuggingFaceApi;
import io.github.uxlabspk.airecommender.api.RetrofitClient;
import io.github.uxlabspk.airecommender.databinding.ActivityMakeupBinding;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;
import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MakeupActivity extends AppCompatActivity {
    private ActivityMakeupBinding binding;
    private static final String API_KEY = "Bearer " + BuildConfig.apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMakeupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Back button functionality
        binding.backButton.setOnClickListener(v -> finish());

        // Recommend button click listener
        binding.recommendButton.setOnClickListener(v -> recommend());

        // Set up dropdown menus for fields mentioned in the XML
        setupAgeDropdown();
        setupBudgetDropdown();
        setupBodyTypeDropdown();    // for skin tone
        setupFitTypeDropdown();     // for skin type
        setupStyleDropdown();       // for makeup style
        setupColorsDropdown();      // for makeup products
        setupOccasionTypeDropdown();
        setupItemsDropdown();       // for shades
        setupAccessoriesDropdown(); // for makeup accessories
    }

    private void recommend() {
        if (!validateFormInputs()) {
            Toast.makeText(this, "All fields are required", Toast.LENGTH_SHORT).show();
            return;
        }

        // Get values from dropdowns
        String age = binding.ageDropdown.getText().toString();
        String budget = binding.budgetDropdown.getText().toString();
        String skinTone = binding.bodyTypeDropdown.getText().toString();
        String skinType = binding.fitTypeDropdown.getText().toString();
        String makeupStyle = binding.styleDropdown.getText().toString();
        String products = binding.colorsDropdown.getText().toString();
        String occasionType = binding.occasionTypeDropdown.getText().toString();
        String shades = binding.itemsDropdown.getText().toString();
        String accessories = binding.accessoriesDropdown.getText().toString();

        // Build prompt for makeup recommendation
        String prompt = buildPrompt(age, budget, skinTone, skinType, makeupStyle, products, occasionType, shades, accessories);

        // Disable the button and show loading text
        binding.recommendButton.setEnabled(false);
        binding.recommendButton.setText(R.string.generating_recommendation);

        String jsonRequest = "{ \"inputs\": \"" + prompt + "\" }";
        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonRequest);

        // showing the progress status
        ProgressStatus progressStatus = new ProgressStatus(this);
        progressStatus.setCanceledOnTouchOutside(false);
        progressStatus.setTitle("Thinking...");
        progressStatus.show();

        HuggingFaceApi apiService = RetrofitClient.getApiService();
        apiService.generateImage(API_KEY, requestBody).enqueue(new Callback<ResponseBody>() {
            private static final int MAX_RETRIES = 2;
            private int retryCount = 0;

            @Override
            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                progressStatus.show();
                binding.recommendButton.setEnabled(true);
                binding.recommendButton.setText(R.string.recommend);

                if (response.isSuccessful() && response.body() != null) {
                    // Convert ResponseBody to an Image
                    try {
                        InputStream inputStream = response.body().byteStream();
                        File file = new File(getCacheDir(), "generated_image" + UUID.randomUUID() + ".png");
                        OutputStream outputStream = null;
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                            outputStream = Files.newOutputStream(file.toPath());
                        } else {
                            outputStream = new FileOutputStream(file); // Fallback for older Android versions
                        }

                        byte[] buffer = new byte[4096]; // Larger buffer for faster transfers
                        int bytesRead;
                        while ((bytesRead = inputStream.read(buffer)) != -1) {
                            outputStream.write(buffer, 0, bytesRead);
                        }

                        outputStream.close();
                        inputStream.close();

                        Intent intent = new Intent(MakeupActivity.this, ResultActivity.class);
                        intent.putExtra("image_path", file.getAbsolutePath());
                        intent.putExtra("prompt", prompt);  // Pass the prompt to result activity
                        startActivity(intent);
                        finish();

                    } catch (Exception e) {
                        Log.e("API_ERROR", "Error processing image: " + e.getMessage());
                        Toast.makeText(MakeupActivity.this, "Error processing image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    int statusCode = response.code();
                    Log.e("API_ERROR", "Response error: " + statusCode + ", " + response.errorBody());

                    if (statusCode == 429 || statusCode == 503 || statusCode == 504) {
                        // These status codes indicate server is busy or timeout
                        retryRequest(call);
                    } else {
                        Toast.makeText(MakeupActivity.this, "Failed to generate recommendation (Error " + statusCode + ")", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                Log.e("API_ERROR", "Request failed: " + t.getMessage());

                if (t instanceof java.io.InterruptedIOException) {
                    retryRequest(call);
                } else {
                    progressStatus.dismiss();
                    binding.recommendButton.setEnabled(true);
                    binding.recommendButton.setText(R.string.recommend);
                    Toast.makeText(MakeupActivity.this, "Network error. Please try again", Toast.LENGTH_SHORT).show();
                }
            }

            private void retryRequest(Call<ResponseBody> call) {
                if (retryCount < MAX_RETRIES) {
                    retryCount++;
                    Log.d("API_RETRY", "Retrying request, attempt " + retryCount);

                    // Show retry message to user
                    Toast.makeText(MakeupActivity.this,
                            "Server busy, retrying (" + retryCount + "/" + MAX_RETRIES + ")...",
                            Toast.LENGTH_SHORT).show();

                    // Exponential backoff - wait longer between each retry
                    long backoffTime = 1000 * (long)Math.pow(2, retryCount);
                    new Handler().postDelayed(() -> {
                        call.clone().enqueue(this);
                    }, backoffTime);
                } else {
                    progressStatus.dismiss();
                    binding.recommendButton.setEnabled(true);
                    binding.recommendButton.setText(R.string.recommend);
                    Toast.makeText(MakeupActivity.this,
                            "Server is currently busy. Please try again later.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });

        // Make API call using HuggingFace API
//        HuggingFaceApi apiService = RetrofitClient.getApiService();
//        apiService.generateImage(API_KEY, requestBody).enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
//                binding.recommendButton.setEnabled(true);
//                binding.recommendButton.setText(R.string.recommend);
//
//                if (response.isSuccessful() && response.body() != null) {
//                    try {
//                        InputStream inputStream = response.body().byteStream();
//                        File file = new File(getCacheDir(), "generated_image" + UUID.randomUUID() + ".png");
//                        OutputStream outputStream = null;
//                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
//                            outputStream = Files.newOutputStream(file.toPath());
//                        }
//
//                        byte[] buffer = new byte[1024];
//                        int bytesRead;
//                        while ((bytesRead = inputStream.read(buffer)) != -1) {
//                            if (outputStream != null)
//                                outputStream.write(buffer, 0, bytesRead);
//                        }
//                        if (outputStream != null)
//                            outputStream.close();
//                        inputStream.close();
//
//                        Intent intent = new Intent(MakeupActivity.this, ResultActivity.class);
//                        intent.putExtra("image_path", file.getAbsolutePath());
//                        intent.putExtra("prompt", prompt);
//                        startActivity(intent);
//
//                    } catch (Exception e) {
//                        Log.e("API_ERROR", "Error processing image: " + e.getMessage());
//                        Toast.makeText(MakeupActivity.this, "Error processing image", Toast.LENGTH_SHORT).show();
//                    }
//                } else {
//                    Log.e("API_ERROR", "Response error: " + response.errorBody());
//                    Toast.makeText(MakeupActivity.this, "Failed to generate recommendation", Toast.LENGTH_SHORT).show();
//                }
//            }
//
//            @Override
//            public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
//                binding.recommendButton.setEnabled(true);
//                binding.recommendButton.setText(R.string.recommend);
//                Log.e("API_ERROR", "Request failed: " + t.getMessage());
//                Toast.makeText(MakeupActivity.this, "Network error. Please try again", Toast.LENGTH_SHORT).show();
//            }
//        });
    }

    private boolean validateFormInputs() {
        return !binding.ageDropdown.getText().toString().isEmpty() &&
                !binding.budgetDropdown.getText().toString().isEmpty() &&
                !binding.bodyTypeDropdown.getText().toString().isEmpty() &&
                !binding.fitTypeDropdown.getText().toString().isEmpty() &&
                !binding.styleDropdown.getText().toString().isEmpty() &&
                !binding.colorsDropdown.getText().toString().isEmpty() &&
                !binding.occasionTypeDropdown.getText().toString().isEmpty() &&
                !binding.itemsDropdown.getText().toString().isEmpty() &&
                !binding.accessoriesDropdown.getText().toString().isEmpty();
    }

    private String buildPrompt(String age, String budget, String skinTone, String skinType, String makeupStyle,
                               String products, String occasionType, String shades, String accessories) {
        return "Generate a personalized makeup recommendation for a person aged " + age +
                " with a budget of " + budget + ". The person has a " + skinTone +
                " skin tone and " + skinType + " skin type. They prefer a " + makeupStyle +
                " makeup look using " + products + " products, suitable for " + occasionType +
                " occasions. Include suggestions for shades (" + shades +
                ") and recommended accessories (" + accessories +
                "). Provide a detailed and creative description.";
    }

    // Setup dropdown for Age
    private void setupAgeDropdown() {
        String[] items = new String[]{"18-24", "25-34", "35-44", "45-54", "55+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.ageDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Budget
    private void setupBudgetDropdown() {
        String[] items = new String[]{"$0-$50", "$50-$100", "$100-$200", "$200-$500", "$500+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.budgetDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Skin Tone (using bodyTypeDropdown)
    private void setupBodyTypeDropdown() {
        String[] items = new String[]{"Fair", "Light", "Medium", "Olive", "Tan", "Deep"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.bodyTypeDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Skin Type (using fitTypeDropdown)
    private void setupFitTypeDropdown() {
        String[] items = new String[]{"Dry", "Oily", "Combination", "Sensitive", "Normal"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.fitTypeDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Makeup Style
    private void setupStyleDropdown() {
        String[] items = new String[]{"Natural", "Glam", "Bold", "Smokey", "Minimal"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.styleDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Makeup Products
    private void setupColorsDropdown() {
        String[] items = new String[]{"Lipsticks", "Foundations", "Eyeshadows", "Blushes", "Mascaras"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.colorsDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Occasion Type
    private void setupOccasionTypeDropdown() {
        String[] items = new String[]{"Everyday", "Party", "Wedding", "Event", "Casual"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.occasionTypeDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Shades
    private void setupItemsDropdown() {
        String[] items = new String[]{"Warm", "Cool", "Neutral"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.itemsDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Makeup Accessories
    private void setupAccessoriesDropdown() {
        String[] items = new String[]{"Brushes", "Sponges", "Applicators", "None"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.accessoriesDropdown.setAdapter(adapter);
    }
}
