package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.databinding.ActivityMainBinding;
import io.github.uxlabspk.airecommender.view.fragments.HomeFragment;
import io.github.uxlabspk.airecommender.view.fragments.ProfileFragment;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        init();
    }

    private void init() {
        binding.navView.setOnItemSelectedListener(item -> {
            Fragment fragment;
            if (item.getItemId() == R.id.item_home) {
                fragment = new HomeFragment();
            } else if (item.getItemId() == R.id.item_profile) {
                fragment = new ProfileFragment();
            } else {
                fragment = new HomeFragment();
            }

            getSupportFragmentManager().beginTransaction()
                    .replace(binding.fragmentContainer.getId(), fragment)
                    .commit();

            return true;
        });
    }
}