package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import io.github.uxlabspk.airecommender.databinding.ActivityLoginBinding;
import io.github.uxlabspk.airecommender.utils.ConfirmDialog;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        // goBack button
        binding.goBack.setOnClickListener(v -> onBackPressed());

        // on login
        binding.loginButton.setOnClickListener(v -> {
            ProgressStatus ps = new ProgressStatus(this);
            ps.setTitle("Authenticating...");
            ps.setCanceledOnTouchOutside(true);
            ps.show();
        });
    }
}