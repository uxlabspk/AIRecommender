package io.github.uxlabspk.airecommender.model;



public class UserModel {
    private String uid;
    private String userName;
    private String userEmail;
    private String userAvatar;

    // For working with firebase.
    public UserModel() {}

    public UserModel(String uid, String userAvatar, String userName, String userEmail) {
        this.uid = uid;
        this.userAvatar = userAvatar;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    public String getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getUserAvatar() {
        return userAvatar;
    }
}
