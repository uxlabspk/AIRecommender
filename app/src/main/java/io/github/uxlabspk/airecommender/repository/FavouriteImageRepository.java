package io.github.uxlabspk.airecommender.repository;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.lifecycle.MutableLiveData;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;

import io.github.uxlabspk.airecommender.api.SupabaseImageUploader;
import io.github.uxlabspk.airecommender.model.ImageModel;

public class FavouriteImageRepository {
    private final MutableLiveData<String> message = new MutableLiveData<>();
    private final MutableLiveData<List<ImageModel>> imageLiveData = new MutableLiveData<>();

    // constructor
    public FavouriteImageRepository(Context context) {
        loadImages(context);
    }

    // save image to firebase storage
    public void saveImage(Context context, String imagePath) {
        File file = new File(imagePath);
        Uri fileUri = Uri.fromFile(file);

        SupabaseImageUploader uploader = new SupabaseImageUploader(context, "img");

        uploader.uploadImage(fileUri, "generated/gen_" + UUID.randomUUID() + ".png", new SupabaseImageUploader.UploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    message.setValue("Upload successful!");
                });
            }

            @Override
            public void onFailure(String errorMessage) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    message.setValue("Upload failed: " + errorMessage);
                });
            }
        });
    }

    // loading the image
    private void loadImages(Context context) {
        SupabaseImageUploader uploader = new SupabaseImageUploader(context, "img");
        uploader.listImagesInBucket(new SupabaseImageUploader.ListImagesCallback() {
            @Override
            public void onSuccess(List<ImageModel> images) {
                if (!images.isEmpty()) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        imageLiveData.setValue(images);
                        Log.d("TAG", "onSuccess: " + images.get(0).getImageUrl());
                    });
                }
            }

            @Override
            public void onFailure(String errorMessage) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Log.e("ImageList", "Failed to list images: " + errorMessage);
                    message.setValue("Failed to list images: " + errorMessage);
                });
            }
        });
    }

    public MutableLiveData<List<ImageModel>> getImageLiveData() {
        return imageLiveData;
    }

    public MutableLiveData<String> getMessage() {
        return message;
    }
}
