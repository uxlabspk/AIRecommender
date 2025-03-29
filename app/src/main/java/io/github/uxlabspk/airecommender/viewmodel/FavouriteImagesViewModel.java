package io.github.uxlabspk.airecommender.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import io.github.uxlabspk.airecommender.model.ImageModel;
import io.github.uxlabspk.airecommender.repository.FavouriteImageRepository;

public class FavouriteImagesViewModel extends ViewModel {
    private final FavouriteImageRepository favouriteImageRepository;


    public FavouriteImagesViewModel() {
        favouriteImageRepository = new FavouriteImageRepository();
    }

    public void saveImage(String imagePath) {
        favouriteImageRepository.saveImage(imagePath);
    }

    public MutableLiveData<List<ImageModel>> getImageLiveData() {
        return favouriteImageRepository.getImageLiveData();
    }

    public MutableLiveData<String> getMessage() {
        return favouriteImageRepository.getMessage();
    }
}
