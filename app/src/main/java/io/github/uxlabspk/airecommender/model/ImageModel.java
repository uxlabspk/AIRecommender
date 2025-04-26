package io.github.uxlabspk.airecommender.model;

public class ImageModel {
    private String imageUrl;

    // Empty constructor required for firebase
    public ImageModel() {}

    public ImageModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

}