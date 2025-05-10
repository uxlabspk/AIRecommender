package io.github.uxlabspk.airecommender.view;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;

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

    private void init() {
        // Ready the Firebase
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
        FirebaseDatabase.getInstance().setPersistenceEnabled(true);
        FirebaseDatabase.getInstance().getReference().keepSynced(true);

        // Check whether the user is login or not
        if (firebaseAuth.getCurrentUser() != null) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }


        // Getting started
        binding.getStartedBtn.setOnClickListener(v -> startActivity(new Intent(IntroductionActivity.this, LoginActivity.class)));
    }
}