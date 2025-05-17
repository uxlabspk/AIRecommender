package io.github.uxlabspk.airecommender.view;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.github.chrisbanes.photoview.PhotoView;

import java.io.File;
import java.io.OutputStream;

import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.databinding.ActivityFashionResultsBinding;
import io.github.uxlabspk.airecommender.viewmodel.FavouriteImagesViewModel;

public class Fashion_results extends AppCompatActivity {

    private ActivityFashionResultsBinding binding;
    private FavouriteImagesViewModel favouriteImagesViewModel;
    private boolean isZoomed = false;
    private String imagePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFashionResultsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        // initialize the view model
        favouriteImagesViewModel = new ViewModelProvider(this).get(FavouriteImagesViewModel.class);
        favouriteImagesViewModel.init(Fashion_results.this);

        // observe the changes
        observe();

        // go back clicked
        binding.goBack.setOnClickListener(view -> finish());

        // setting the prompt under image
        binding.noticeText.setText(getIntent().getStringExtra("prompt"));

        // Get image path from intent or use default
        imagePath = getIntent().getStringExtra("image_path");
        if (imagePath == null || imagePath.isEmpty()) {
            imagePath = "https://firebasestorage.googleapis.com/v0/b/syncbox-c24b7.appspot.com/o/images%2FbgmWtjWkkpPcxbVIvff5R2rOt2q1%2Fgenerated_image22d6eecc-de7c-4bd3-aff4-71e0c52b23e0.jpg?alt=media&token=0d39de99-8607-44d6-902b-dc49d5e6cde0";
        }

        // Load the image into the PhotoView
        Glide.with(this)
                .load(imagePath)
                .centerCrop()
                .into(binding.fullView);

        // Set scale change listener to toggle UI visibility
        binding.fullView.setOnScaleChangeListener((scaleFactor, focusX, focusY) -> {
            if (scaleFactor > 1.0f) {
                // Hide UI elements when zoomed in
                binding.buttonsLayout.setVisibility(View.GONE);
                binding.noticeText.setVisibility(View.GONE);
                isZoomed = true;
            } else {
                // Show UI elements when at normal zoom
                binding.buttonsLayout.setVisibility(View.VISIBLE);
                binding.noticeText.setVisibility(View.VISIBLE);
                isZoomed = false;
            }
        });

        // Set single tap listener to toggle UI visibility
        binding.fullView.setOnClickListener(view -> {
            if (isZoomed) {
                // Toggle UI visibility on tap when zoomed
                if (binding.buttonsLayout.getVisibility() == View.VISIBLE) {
                    binding.buttonsLayout.setVisibility(View.GONE);
                    binding.noticeText.setVisibility(View.GONE);
                } else {
                    binding.buttonsLayout.setVisibility(View.VISIBLE);
                    binding.noticeText.setVisibility(View.VISIBLE);
                }
            }
        });

        // on download button clicked
        binding.downloadBtn.setOnClickListener(view -> saveImageToGalleryModern(imagePath));

        // on favourite button clicked
        binding.favouriteButton.setOnClickListener(view -> {
            binding.favouriteButton.setImageDrawable(getDrawable(R.drawable.ic_filled_heart));
            new Handler(Looper.getMainLooper()).postDelayed(() -> binding.favouriteButton.setVisibility(View.GONE), 3000);
            favouriteImagesViewModel.saveImage(Fashion_results.this, imagePath);
        });
    }

    private void observe() {
        favouriteImagesViewModel.getMessage().observe(this, message -> {
            if (message != null) {
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void saveImageToGalleryModern(String imagePath) {
        try {
            // Create a file object from the image path
            File sourceFile = new File(imagePath);
            if (!sourceFile.exists()) {
                Toast.makeText(this, "Source image doesn't exist", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get bitmap from file
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());

            // Create values for the new image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            // Insert the image
            ContentResolver resolver = getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                // Open output stream and save bitmap
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    // Notify user
                    Toast.makeText(this, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}