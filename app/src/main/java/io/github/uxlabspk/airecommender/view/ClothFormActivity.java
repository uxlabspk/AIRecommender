package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.github.uxlabspk.airecommender.R;

public class ClothFormActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cloth_form);

        // Initialize back button
        ImageButton backButton = findViewById(R.id.backButton);
        backButton.setOnClickListener(v -> finish());

        // Initialize recommend button
        Button recommendButton = findViewById(R.id.recommendButton);
        recommendButton.setOnClickListener(v -> {
            // Implement recommendation logic
            Toast.makeText(this, "Generating recommendations...", Toast.LENGTH_SHORT).show();
        });

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

    private void setupAgeDropdown() {
        String[] items = new String[]{"18-24", "25-34", "35-44", "45-54", "55+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.ageDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupGenderDropdown() {
        String[] items = new String[]{"Male", "Female", "Non-binary", "Prefer not to say"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.genderDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupBudgetDropdown() {
        String[] items = new String[]{"$0-$50", "$50-$100", "$100-$200", "$200-$500", "$500+"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.budgetDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupBodyTypeDropdown() {
        String[] items = new String[]{"Slim", "Athletic", "Average", "Curvy", "Plus Size"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.bodyTypeDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupFitTypeDropdown() {
        String[] items = new String[]{"Slim Fit", "Regular Fit", "Relaxed Fit", "Oversized"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.fitTypeDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupHeightDropdown() {
        String[] items = new String[]{"<5'0\"", "5'0\"-5'4\"", "5'5\"-5'8\"", "5'9\"-6'0\"", ">6'0\""};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.heightDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupWeightDropdown() {
        String[] items = new String[]{"<120 lbs", "120-150 lbs", "151-180 lbs", "181-210 lbs", ">210 lbs"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.weightDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupSizeDropdown() {
        String[] items = new String[]{"XS", "S", "M", "L", "XL", "XXL"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.sizeDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupStyleDropdown() {
        String[] items = new String[]{"Casual", "Formal", "Business", "Sporty", "Bohemian", "Vintage"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.styleDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupColorsDropdown() {
        String[] items = new String[]{"Neutrals", "Bright", "Pastels", "Earth Tones", "Monochrome"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.colorsDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupFabricDropdown() {
        String[] items = new String[]{"Cotton", "Linen", "Silk", "Wool", "Synthetic", "Denim"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.fabricDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupOccasionTypeDropdown() {
        String[] items = new String[]{"Everyday", "Work", "Date Night", "Party", "Wedding", "Vacation"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.occasionTypeDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupItemsDropdown() {
        String[] items = new String[]{"Shirts", "Pants", "Dresses", "Skirts", "Outerwear", "Accessories"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.itemsDropdown);
        dropdown.setAdapter(adapter);
    }

    private void setupPreferenceDropdown() {
        String[] items = new String[]{"Trendy", "Classic", "Sustainable", "Budget-friendly", "Designer"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, R.layout.dropdown_item, items);
        AutoCompleteTextView dropdown = findViewById(R.id.preferenceDropdown);
        dropdown.setAdapter(adapter);
    }
}