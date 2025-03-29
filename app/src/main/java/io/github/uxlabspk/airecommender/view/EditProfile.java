package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

import java.util.Objects;

import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.databinding.ActivityEditProfileBinding;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;
import io.github.uxlabspk.airecommender.viewmodel.AuthViewModel;

public class EditProfile extends AppCompatActivity {

    private ActivityEditProfileBinding binding;
    private AuthViewModel authViewModel;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;
    private ProgressStatus progressStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityEditProfileBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        // initializing the progress status indicator
        progressStatus = new ProgressStatus(this);
        progressStatus.setTitle("Updating Profile");
        progressStatus.setCanceledOnTouchOutside(false);

        // initializing the auth view model
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // fetching the login user information
        authViewModel.fetchUserInformation();

        // observe the live data
        observer();

        // go Back button
        binding.goBack.setOnClickListener(view -> finish());

        // update user profile image
        binding.profilePic.setOnClickListener(view -> selectImage());

        // reset password clicked
        binding.resetPassword.setOnClickListener(view -> {
            FirebaseAuth auth = FirebaseAuth.getInstance();
            authViewModel.resetPassword(Objects.requireNonNull(auth.getCurrentUser()).getEmail());
        });
        
        // update the user profile
        binding.updateButton.setOnClickListener(view -> updateProfile());
    }

    private void updateProfile() {
        progressStatus.show();
        Log.d("URI", "updateProfile: " + filePath);
//        authViewModel.updateUser(filePath, binding.userName.getText().toString(), binding.userEmail.getText().toString());
        authViewModel.updateUser(filePath, binding.userName.getText().toString());
    }

    private void observer() {
        authViewModel.getUserModelLiveData().observe(this, user -> {
            binding.userName.setText(user.getUserName());
            //binding.userEmail.setText(user.getUserEmail());
            if (user.getUserAvatar().isEmpty()) binding.profilePic.setImageResource(R.drawable.ic_user);
            else Glide.with(this).load(user.getUserAvatar()).into(binding.profilePic);
        });

        // observing the success messages
        authViewModel.getSuccessLiveData().observe(this, message -> {
            if (message != null) {
                progressStatus.dismiss();
                Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
            }
        });

        // observing the error message
        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) {
                progressStatus.dismiss();
                Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void selectImage() {
        // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent,"Select Image from here..."), PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (
                requestCode == PICK_IMAGE_REQUEST
                && resultCode == RESULT_OK
                && data != null 
                && data.getData() != null
        ) {
            // Get the Uri of data
            filePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                binding.profilePic.setImageBitmap(bitmap);
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}