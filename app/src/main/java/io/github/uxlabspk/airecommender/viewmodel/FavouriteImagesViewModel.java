package io.github.uxlabspk.airecommender.viewmodel;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.github.uxlabspk.airecommender.model.ImageModel;
import io.github.uxlabspk.airecommender.repository.FavouriteImageRepository;

public class FavouriteImagesViewModel extends ViewModel {
    private FavouriteImageRepository favouriteImageRepository;


    public FavouriteImagesViewModel() {
    }

    public void init(Context context) {
        favouriteImageRepository = new FavouriteImageRepository(context);
    }

    public void saveImage(Context context, String imagePath) {
        favouriteImageRepository.saveImage(context, imagePath);
    }

    public MutableLiveData<List<ImageModel>> getImageLiveData() {
        return favouriteImageRepository.getImageLiveData();
    }

    public MutableLiveData<String> getMessage() {
        return favouriteImageRepository.getMessage();
    }
}
