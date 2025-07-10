package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.graphics.Bitmap;
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
import io.github.uxlabspk.airecommender.api.ImageRequest;
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
        String occasionType = binding.occasionTypeDropdown.getText().toString();
        String shades = binding.itemsDropdown.getText().toString();
        String accessories = binding.accessoriesDropdown.getText().toString();

        // Build prompt for makeup recommendation
        String prompt = buildPrompt(age, budget, skinTone, skinType, makeupStyle, occasionType, shades, accessories);

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

        String url = "https://router.huggingface.co/fal-ai/fal-ai/flux-lora";
        String authToken = API_KEY;

        ImageRequest.fetchImageWithPost(url, prompt, authToken, new ImageRequest.ImageCallback() {
            @Override
            public void onSuccess(Bitmap image) {
                runOnUiThread(() -> {
                    File file = new File(getExternalFilesDir(null), "generated_image" + UUID.randomUUID() + ".jpg");
                    try {
                        FileOutputStream out = new FileOutputStream(file);
                        image.compress(Bitmap.CompressFormat.JPEG, 100, out);
                        out.flush();
                        out.close();

                        // stopping progress indicator
                        progressStatus.dismiss();

                        Intent intent = new Intent(MakeupActivity.this, ResultActivity.class);
                        intent.putExtra("image_path", file.getAbsolutePath());
                        intent.putExtra("prompt", prompt);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        progressStatus.dismiss();
                        e.printStackTrace();
                        Toast.makeText(MakeupActivity.this, "Error saving image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressStatus.dismiss();
                    Toast.makeText(MakeupActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        });
    }

    private boolean validateFormInputs() {
        return !binding.ageDropdown.getText().toString().isEmpty() &&
                !binding.budgetDropdown.getText().toString().isEmpty() &&
                !binding.bodyTypeDropdown.getText().toString().isEmpty() &&
                !binding.fitTypeDropdown.getText().toString().isEmpty() &&
                !binding.styleDropdown.getText().toString().isEmpty() &&
                !binding.occasionTypeDropdown.getText().toString().isEmpty() &&
                !binding.itemsDropdown.getText().toString().isEmpty() &&
                !binding.accessoriesDropdown.getText().toString().isEmpty();
    }

    private String buildPrompt(String age, String budget, String skinTone, String skinType, String makeupStyle,
                               String occasionType, String shades, String accessories) {
        return "Photorealistic close-up portrait of a model with " + skinTone +
                " skin tone wearing professional " + makeupStyle + " makeup look. " +
                "The makeup should be suitable for a person aged " + age +
                " with " + skinType + " skin type attending a " + occasionType + " event. " +
                "Highlight " + shades + " color palette and include " + accessories + " as accessories. " +
                "The makeup application should be visible in detail - eyeshadow, lipstick, foundation, blush, and contour. " +
                "Professional beauty photography with soft, flattering lighting, high resolution, detailed textures. " +
                "The image should showcase the makeup techniques that would work within a " + budget + " budget. " +
                "Clean background, professional makeup artistry, modern beauty standards, glamour photography.";
    }

    // Setup dropdown for Age
    private void setupAgeDropdown() {
        String[] items = new String[]{"18-24", "25-34", "35-44", "45-54", "55+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, R.layout.dropdown_item, items);
        binding.ageDropdown.setAdapter(adapter);
    }

    // Setup dropdown for Budget
    private void setupBudgetDropdown() {
        String[] items = new String[]{"PKR 0-15,000",
                "PKR 15,000-30,000",
                "PKR 30,000-60,000",
                "PKR 60,000-150,000",
                "PKR 150,000+"};
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
