package io.github.uxlabspk.airecommender.model;



public class UserModel {
    private String uid;
    private String userName;
    private String userEmail;

    // For working with firebase.
    public UserModel() {}

    public UserModel(String uid, String userName, String userEmail) {
        this.uid = uid;
        this.userName = userName;
        this.userEmail = userEmail;
    }

    // Getters
    public String getUid() {
        return uid;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserEmail() {
        return userEmail;
    }
}
