package io.github.uxlabspk.airecommender.model;

public class ImageModel {
    private String imageUrl;

    public ImageModel() {}

    public ImageModel(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}