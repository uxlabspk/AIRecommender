package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import io.github.uxlabspk.airecommender.databinding.ActivitySignupBinding;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;
import io.github.uxlabspk.airecommender.viewmodel.AuthViewModel;

public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    private AuthViewModel authViewModel;
    private ProgressStatus ps;
    private Uri filePath;
    private final int PICK_IMAGE_REQUEST = 22;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        // observe the changes
        observeChanges();

        // Ready the progress status
        ps = new ProgressStatus(this);
        ps.setTitle("Authenticating...");
        ps.setCanceledOnTouchOutside(true);

        // go back listener
        binding.goBack.setOnClickListener(v -> finish());

        // on avatar selection
        binding.userAvatar.setOnClickListener(view -> selectImage());

        // on already account click
        binding.alreadyAccountLink.setOnClickListener(v -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        });

        // login button
        binding.signupBtn.setOnClickListener(v -> {
            String userName = binding.editTextNameAddress.getText().toString();
            String userEmail = binding.editTextEmail.getText().toString();
            String userPassword = binding.userPassword.getText().toString();

            createUser(userEmail, userPassword, userName);
        });
    }

    private void observeChanges() {
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                ps.dismiss();
                startActivity(new Intent(SignupActivity.this, MainActivity.class));
                finish();
            }
        });

        // getting error data
        authViewModel.getErrorLiveData().observe(this, error -> {
            ps.dismiss();
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });

        // getting success data
        authViewModel.getSuccessLiveData().observe(this, successMessage -> {
            if (successMessage != null) {
                Toast.makeText(this, successMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createUser(String email, String password, String userName) {
        // validate the user input
        String emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+";

        if (!email.matches(emailPattern)) {
            binding.emailLayout.setError("Invalid Email Address");
        } else if (password.isEmpty() || password.length() < 6) {
            binding.passwordLayout.setError("Password must be 6 digit long");
        } else if (userName.isEmpty()) {
            binding.nameLayout.setError("Invalid Username");
        } else {
            // removing errors
            binding.editTextNameAddress.setError("");
            binding.emailLayout.setError("");
            binding.passwordLayout.setError("");

            // Showing the Progress Dialog
            ps.show();

            // create the user
            authViewModel.registerUser(filePath, userName, email, password);
        }
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
                binding.userAvatar.setImageBitmap(bitmap);
            }

            catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}