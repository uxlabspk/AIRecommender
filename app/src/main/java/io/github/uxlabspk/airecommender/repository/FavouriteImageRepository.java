package io.github.uxlabspk.airecommender.repository;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.sql.Time;
import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import io.github.uxlabspk.airecommender.model.ImageModel;

public class FavouriteImageRepository {
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<ImageModel>> imageLiveData = new MutableLiveData<>();

    // constructor
    public FavouriteImageRepository() {
        loadImages();
    }

    // save image to firebase storage
    public void saveImage(Context context, String imagePath) {
        File file = new File(imagePath);
        Uri fileUri = Uri.fromFile(file);

        SupabaseImageUploader uploader = new SupabaseImageUploader(context, "img");

        uploader.uploadImage(fileUri, "generated/gen_" + UUID.randomUUID() + ".png", new SupabaseImageUploader.UploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                message.setValue("Upload successful!");
            }

            @Override
            public void onFailure(String errorMessage) {
                message.setValue("Upload failed: " + errorMessage);
            }
        });
    }

    // loading the image
    private void loadImages() {
//        SupabaseImageUploader uploader = new SupabaseImageUploader(context, "img");

//        uploader.listAllImages("generated");

//        StorageReference storageRef = storage.getReference().child("images/" + Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid() + "/");
//
//        storageRef.listAll()
//                .addOnSuccessListener(listResult -> {
//                    if (listResult.getItems().isEmpty()) {
//                        message.setValue("Not Found");
//                    }
//                    List<ImageModel> imageList = new ArrayList<>();
//                    for (StorageReference fileRef : listResult.getItems()) {
//                        fileRef.getDownloadUrl().addOnSuccessListener(uri -> {
//                            if (uri != null) {
//                                imageList.add(new ImageModel(uri.toString()));
//                                imageLiveData.setValue(imageList); // Update LiveData after each fetch
//                            }
//                        });
//                    }
//                })
//                .addOnFailureListener(e -> message.setValue(e.getMessage()));
    }

    public MutableLiveData<List<ImageModel>> getImageLiveData() {
        return imageLiveData;
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }
}
