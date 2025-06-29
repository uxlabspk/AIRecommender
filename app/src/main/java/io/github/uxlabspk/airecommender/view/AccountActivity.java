package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import io.github.uxlabspk.airecommender.databinding.ActivityAccountBinding;
import io.github.uxlabspk.airecommender.viewmodel.AuthViewModel;

public class AccountActivity extends AppCompatActivity {

    private ActivityAccountBinding binding;
    private AuthViewModel authViewModel;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAccountBinding.inflate(getLayoutInflater());
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        // observe the changes
        observeChanges();

        // sign in button
        binding.signInBtn.setOnClickListener(v -> startActivity(new Intent(AccountActivity.this, LoginActivity.class)));

        // sign up button
        binding.signUpBtn.setOnClickListener(v -> startActivity(new Intent(AccountActivity.this, SignupActivity.class)));
    }

    private void observeChanges() {
        authViewModel.getUserLiveData().observe(this, user -> {
            if (user != null) {
                startActivity(new Intent(this, MainActivity.class));
                finish();
            }
        });

        authViewModel.getErrorLiveData().observe(this, error -> {
            if (error != null) Toast.makeText(this, error, Toast.LENGTH_SHORT).show();
        });
    }
}