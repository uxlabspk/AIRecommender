package io.github.uxlabspk.airecommender.viewmodel;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import io.github.uxlabspk.airecommender.model.ImageModel;

public class FavouriteImagesViewModel extends ViewModel {
    private final MutableLiveData<List<ImageModel>> imageLiveData = new MutableLiveData<>();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();
    private final StorageReference storageRef = storage.getReference().child("images/").child("favourite/");

    // constructor
    public FavouriteImagesViewModel() {
        loadImages();
    }

    // loading the image
    private void loadImages() {
        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    List<ImageModel> imageList = new ArrayList<>();
                    for (StorageReference fileRef : listResult.getItems()) {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            imageList.add(new ImageModel(uri.toString()));
                            imageLiveData.setValue(imageList); // Update LiveData after each fetch
                        });
                    }
                })
                .addOnFailureListener(e -> imageLiveData.setValue(null));
    }

    public MutableLiveData<List<ImageModel>> getImageLiveData() {
        return imageLiveData;
    }
}
