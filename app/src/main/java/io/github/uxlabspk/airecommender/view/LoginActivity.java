    package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import com.google.android.gms.common.api.ApiException;
import com.google.firebase.auth.FirebaseUser;

import io.github.uxlabspk.airecommender.databinding.ActivityLoginBinding;
import io.github.uxlabspk.airecommender.utils.ConfirmDialog;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;
import io.github.uxlabspk.airecommender.viewmodel.AuthViewModel;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private AuthViewModel authViewModel;
    private ProgressStatus ps;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
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

        // goBack button
        binding.goBack.setOnClickListener(v -> onBackPressed());

        // on forget password click
        binding.resetPassword.setOnClickListener(v -> {
            if (!binding.signinUserEmail.getText().toString().isEmpty()) {
                authViewModel.resetPassword(binding.signinUserEmail.getText().toString());
            }
            else
                Toast.makeText(this, "Please Enter Valid Email Address", Toast.LENGTH_SHORT).show();
        });

        // on login
        binding.loginButton.setOnClickListener(v -> {
            authenticateUser(binding.signinUserEmail.getText().toString(), binding.signinUserPassword.getText().toString());
        });

        // create account link
        binding.createAccountLink.setOnClickListener(v -> startActivity(new Intent(this, SignupActivity.class)));
    }

    private void observeChanges() {
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                ps.dismiss();
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
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

    private void authenticateUser(String email, String password) {
        // validate the user input
        String emailPattern = "[a-zA-Z\\d._-]+@[a-z]+\\.+[a-z]+";

        if (!email.matches(emailPattern)) {
            binding.emailLayout.setError("Invalid Email Address");
        } else if (password.isEmpty() || password.length() < 6) {
            binding.passwordLayout.setError("Password must be 6 digit long");
        } else {
            // removing errors
            binding.emailLayout.setError("");
            binding.passwordLayout.setError("");

            // Showing the Progress Dialog
            ps.show();

            // login the user
            authViewModel.loginUser(email, password);
        }
    }
}