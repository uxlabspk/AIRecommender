package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.ismaeldivita.chipnavigation.ChipNavigationBar;

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

        init(savedInstanceState);
    }

    private void init(Bundle savedInstanceState) {
        // On UI refresh, No transaction will occur.
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }

        binding.navView.setOnItemSelectedListener(new ChipNavigationBar.OnItemSelectedListener() {
            @Override
            public void onItemSelected(int id) {
                Fragment fragment = null;
                if (id == R.id.item_home) {
                    fragment = new HomeFragment();
                } else if (id == R.id.item_profile) {
                    fragment = new ProfileFragment();
                }

                if (fragment != null) {
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, fragment)
                            .commit();
                } else {
                    Log.e("TAG", "Error in creating fragment");
                }
            }
        });
    }
}