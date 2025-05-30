package io.github.uxlabspk.airecommender.view.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import io.github.uxlabspk.airecommender.databinding.FragmentHomeBinding;
import io.github.uxlabspk.airecommender.view.ClothFormActivity;
import io.github.uxlabspk.airecommender.view.FashionStyleActivity;
import io.github.uxlabspk.airecommender.view.MakeupActivity;

public class HomeFragment extends Fragment {
    private FragmentHomeBinding binding;

    public HomeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout using View Binding
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot(); // Return the root view from the binding
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Set up the click listener using View Binding
        binding.clothForm.setOnClickListener(v -> {
            // Create an Intent to start AnotherActivity
            Intent intent = new Intent(requireContext(), ClothFormActivity.class);
            startActivity(intent);
        });

        // Set up the click listener using View Binding
        binding.makeupForm.setOnClickListener(v -> {
            // Create an Intent to start AnotherActivity
            Intent intent = new Intent(requireContext(), MakeupActivity.class);
            startActivity(intent);
        });

        // Set up the click listener using View Binding
        binding.fashionStyle.setOnClickListener(v -> {
            // Create an Intent to start AnotherActivity
            Intent intent = new Intent(requireContext(), FashionStyleActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null; // Release the binding when the view is destroyed
    }
}