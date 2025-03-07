package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.databinding.ActivityIntroductionBinding;

public class IntroductionActivity extends AppCompatActivity {

    private ActivityIntroductionBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityIntroductionBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {}
}