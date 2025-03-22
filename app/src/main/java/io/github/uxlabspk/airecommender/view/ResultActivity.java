package io.github.uxlabspk.airecommender.view;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.OutputStream;

import io.github.uxlabspk.airecommender.databinding.ActivityResultBinding;

public class ResultActivity extends AppCompatActivity {
    ActivityResultBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityResultBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        // go back clicked
        binding.goBack.setOnClickListener(view -> finish());

        // loading the image
        String imagePath = getIntent().getStringExtra("image_path");
        if (imagePath != null) {
            File imgFile = new File(imagePath);
            if (imgFile.exists()) {
                Glide.with(this)
                        .load(imgFile)
                        .fitCenter()
                        .into(binding.previewImage);
            }
        }

        // on favourite button clicked
        binding.favouriteButton.setOnClickListener(view -> saveImageToGalleryModern(imagePath));
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
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }
}