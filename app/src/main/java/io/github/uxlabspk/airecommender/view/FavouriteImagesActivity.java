package io.github.uxlabspk.airecommender.view;

import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import io.github.uxlabspk.airecommender.databinding.ActivityFavouriteImagesBinding;
import io.github.uxlabspk.airecommender.model.ImageModel;
import io.github.uxlabspk.airecommender.view.Adapters.ImageAdapter;
import io.github.uxlabspk.airecommender.viewmodel.FavouriteImagesViewModel;

public class FavouriteImagesActivity extends AppCompatActivity {

    private ActivityFavouriteImagesBinding binding;
    private ImageAdapter imageAdapter;
    private List<ImageModel> imageList;
    private FavouriteImagesViewModel viewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFavouriteImagesBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        init();
    }

    private void init() {
        // initializing the view model
        viewModel = new ViewModelProvider(this).get(FavouriteImagesViewModel.class);

        // Setting up the Recycler View
        binding.favouriteImagesRv.setLayoutManager(new LinearLayoutManager(this));

        imageList = new ArrayList<>();
        imageAdapter = new ImageAdapter(this, imageList);
        binding.favouriteImagesRv.setAdapter(imageAdapter);

        // Observe LiveData
        observe();
    }

    private void observe() {
        viewModel.getImageLiveData().observe(this, images -> {
            if (images != null) {
                imageList.clear();
                imageList.addAll(images);
                imageAdapter.notifyDataSetChanged();
            } else {
                binding.noRecordsFoundLayout.setVisibility(View.VISIBLE);
            }
        });
    }
}