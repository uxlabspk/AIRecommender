package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.util.UUID;

import io.github.uxlabspk.airecommender.BuildConfig;
import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.api.ImageRequest;
import io.github.uxlabspk.airecommender.databinding.ActivityClothFormBinding;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;


public class ClothFormActivity extends AppCompatActivity {
    private ActivityClothFormBinding binding;
    private static final String API_KEY = "Bearer " + BuildConfig.apiKey;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityClothFormBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // on back button clicked
        binding.backButton.setOnClickListener(v -> finish());

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
        binding.recommendButton.setText(R.string.generating_recommendation);

        // Create JSON request body
//        String jsonRequest = "{ \"inputs\": \"" + prompt + "\" }";
//        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json"), jsonRequest);

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

                        Intent intent = new Intent(ClothFormActivity.this, ResultActivity.class);
                        intent.putExtra("image_path", file.getAbsolutePath());
                        intent.putExtra("prompt", prompt);
                        startActivity(intent);
                        finish();
                    } catch (Exception e) {
                        progressStatus.dismiss();
                        e.printStackTrace();
                        Toast.makeText(ClothFormActivity.this, "Error saving image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressStatus.dismiss();
                    Toast.makeText(ClothFormActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
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

        return "Full-body photorealistic image of a " + gender + " model wearing traditional Pakistani-Islamic clothing. " +
                "A " + age + "-year-old with " + bodyType + " body type, " + height + " height, " +
                "in size " + size + ". Show detailed " + style + " style outfit in " + colors + " colors, " +
                "made from " + fabric + " fabric. The outfit includes " + items + " with " + fitType + " fit, " +
                "perfect for " + occasionType + " occasions. Budget range " + budget + ". " +
                "Highlight modest Islamic aesthetics with traditional embroidered shalwar kameez, " +
                "properly draped dupatta/hijab, and culturally appropriate accessories. " +
                "The model should be standing, showing the complete outfit from head to toe. " +
                "Professional fashion photography style, clear lighting, high-resolution, detailed textures.";
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
        String[] items = new String[]{"PKR 0-15,000",
                "PKR 15,000-30,000",
                "PKR 30,000-60,000",
                "PKR 60,000-150,000",
                "PKR 150,000+"};
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
        String[] items = new String[]{"Slim Fit", "Regular Fit", "Relaxed Fit", "Oversize"};
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
        String[] items = new String[]{"<54.4 kg",
                "54.4–68.0 kg",
                "68.5–81.6 kg",
                "82.1–95.3 kg",
                ">95.3 kg"};
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
        String[] items = new String[]{"Shirts", "Pants", "Dresses", "Shalwar Qameez", "Skirts", "Outerwear", "Accessories"};
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