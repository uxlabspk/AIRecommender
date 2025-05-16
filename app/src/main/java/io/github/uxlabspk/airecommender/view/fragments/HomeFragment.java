package io.github.uxlabspk.airecommender.view.fragments;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;

import io.github.uxlabspk.airecommender.databinding.FragmentHomeBinding;
import io.github.uxlabspk.airecommender.repository.SupabaseImageUploader;
import io.github.uxlabspk.airecommender.view.ClothFormActivity;
import io.github.uxlabspk.airecommender.view.MakeupActivity;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    private static final int PERMISSION_REQUEST_READ_EXTERNAL_STORAGE = 101;
    private Uri selectedImageUri;

    // Modern way to handle activity results using ActivityResultLauncher
    private final ActivityResultLauncher<Intent> galleryLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    selectedImageUri = result.getData().getData();
                    uploadImg();
                }
            });

    // Modern way to handle permissions
    private final ActivityResultLauncher<String> requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    openGallery();
                } else {
                    Toast.makeText(getContext(), "Permission denied. Cannot access gallery.", Toast.LENGTH_SHORT).show();
                }
            });

    private void checkPermissionAndOpenGallery() {
        // Check if we have permission to read external storage
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            // Permission already granted, open gallery
            openGallery();
        } else {
            // Request the permission
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE);
        }
    }

    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        galleryLauncher.launch(intent);
    }

    // For handling the image URI - you might want to save this in your ViewModel or save state
    public Uri getSelectedImageUri() {
        return selectedImageUri;
    }

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Return the root view from the binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the click listener using View Binding
        binding.clothForm.setOnClickListener(v -> {
            // Create an Intent to start AnotherActivity
            Intent intent = new Intent(requireContext(), ClothFormActivity.class);
            startActivity(intent);
        });

        // Set up the click listener using View Binding
        binding.makeupForm.setOnClickListener(v -> {
            // Create an Intent to start AnotherActivity
            Intent intent = new Intent(requireContext(), MakeupActivity.class);
            startActivity(intent);
        });

        // Set up the click listener using View Binding
        binding.fashionStyle.setOnClickListener(v -> {
            // Create an Intent to start AnotherActivity
//            Intent intent = new Intent(requireContext(), FashionStyleActivity.class);
//            startActivity(intent);

//            String response = AppWriteClient.getDocuments("ran");
//            Log.d("AppwriteGET", response);

//            String jsonData = "{\"name\":\"John Doe\", \"email\":\"john@example.com\"}";
//            String postResponse = AppWriteClient.createDocument("ran", "unique()", jsonData);
//            Log.d("AppwritePOST", postResponse);

            // Initialize the uploader (typically in your Activity or Fragment)

            openGallery();


        });
    }

    private void uploadImg() {
        SupabaseImageUploader uploader = new SupabaseImageUploader(
                getContext(),
                "img"
        );

        // Upload an image from gallery (after you've selected an image and have its Uri)
        uploader.uploadImage(selectedImageUri, "",  new SupabaseImageUploader.UploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                // Handle successful upload - fileUrl is the public URL to your uploaded image
                Log.d("ImageUpload", "Success! URL: " + fileUrl);
            }

            @Override
            public void onFailure(String errorMessage) {
                // Handle failed upload
                Log.e("ImageUpload", "Failed: " + errorMessage);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release the binding when the view is destroyed
    }
}