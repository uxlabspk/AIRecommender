package io.github.uxlabspk.airecommender.repository;

import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.github.uxlabspk.airecommender.model.ImageModel;

public class FavouriteImageRepository {
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<ImageModel>> imageLiveData = new MutableLiveData<>();
    private final FirebaseStorage storage = FirebaseStorage.getInstance();

    // constructor
    public FavouriteImageRepository() {
        loadImages();
    }

    // save image to firebase storage
    public void saveImage(String imagePath) {
        File file = new File(imagePath);
        Uri fileUri = Uri.fromFile(file);

        // Get Firebase Storage reference
        StorageReference storageRef = storage.getReference().child("images/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/" + file.getName());

        // Upload the file
        storageRef.putFile(fileUri)
                .addOnSuccessListener(taskSnapshot -> message.setValue("Upload successful!"))
                .addOnFailureListener(e -> message.setValue("Upload failed: " + e.getMessage()));
    }

    // loading the image
    private void loadImages() {
        StorageReference storageRef = storage.getReference().child("images/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/");

        storageRef.listAll()
                .addOnSuccessListener(listResult -> {
                    if (listResult.getItems().isEmpty()) {
                        message.setValue("Not Found");
                    }
                    List<ImageModel> imageList = new ArrayList<>();
                    for (StorageReference fileRef : listResult.getItems()) {
                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
                            if (uri != null) {
                                imageList.add(new ImageModel(uri.toString()));
                                imageLiveData.setValue(imageList); // Update LiveData after each fetch
                            }
                        });
                    }
                })
                .addOnFailureListener(e -> message.setValue(e.getMessage()));
    }

    public MutableLiveData<List<ImageModel>> getImageLiveData() {
        return imageLiveData;
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }
}
