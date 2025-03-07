package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import io.github.uxlabspk.airecommender.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {}
}