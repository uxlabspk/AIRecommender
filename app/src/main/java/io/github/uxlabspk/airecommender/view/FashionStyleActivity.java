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

import com.google.gson.Gson;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import io.github.uxlabspk.airecommender.BuildConfig;
import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.api.ImageRequest;
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
                new String[]{"<54.4 kg",
                        "54.4–68.0 kg",
                        "68.5–81.6 kg",
                        "82.1–95.3 kg",
                        ">95.3 kg"}));

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
        if (!validateFormInputs()) {
            Toast.makeText(this, "Please fill in all fields before proceeding.", Toast.LENGTH_LONG).show();
            return;
        }

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

                        Intent intent = new Intent(FashionStyleActivity.this, Fashion_results.class);
                        intent.putExtra("image_path", file.getAbsolutePath());
                        intent.putExtra("prompt", prompt);
                        startActivity(intent);

                    } catch (Exception e) {
                        progressStatus.dismiss();
                        e.printStackTrace();
                        Toast.makeText(FashionStyleActivity.this, "Error saving image", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onError(Exception e) {
                runOnUiThread(() -> {
                    progressStatus.dismiss();
                    Toast.makeText(FashionStyleActivity.this,
                            "Error: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
                e.printStackTrace();
            }
        });
    }

    private boolean validateFormInputs() {
        return !binding.ageDropdown.getText().toString().isEmpty() &&
                !binding.genderDropdown.getText().toString().isEmpty() &&
                !binding.budgetDropdown.getText().toString().isEmpty() &&
                !binding.bodyTypeDropdown.getText().toString().isEmpty() &&
                !binding.fitTypeDropdown.getText().toString().isEmpty() &&
                !binding.heightDropdown.getText().toString().isEmpty() &&
                !binding.weightDropdown.getText().toString().isEmpty() &&
                !binding.sizeDropdown.getText().toString().isEmpty() &&
                !binding.skinToneDropdown.getText().toString().isEmpty() &&
                !binding.skinTypeDropdown.getText().toString().isEmpty() &&
                !binding.clothingStyleDropdown.getText().toString().isEmpty() &&
                !binding.makeupStyleDropdown.getText().toString().isEmpty() &&
                !binding.colorsDropdown.getText().toString().isEmpty() &&
                !binding.fabricDropdown.getText().toString().isEmpty() &&
                !binding.makeupProductsDropdown.getText().toString().isEmpty() &&
                !binding.occasionTypeDropdown.getText().toString().isEmpty() &&
                !binding.itemsDropdown.getText().toString().isEmpty() &&
                !binding.preferenceDropdown.getText().toString().isEmpty() &&
                !binding.shadesDropdown.getText().toString().isEmpty() &&
                !binding.accessoriesDropdown.getText().toString().isEmpty();
    }

}
