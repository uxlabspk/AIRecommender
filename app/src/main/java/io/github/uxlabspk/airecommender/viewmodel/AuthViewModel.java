package io.github.uxlabspk.airecommender.viewmodel;

import android.content.Context;
import android.net.Uri;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;

import io.github.uxlabspk.airecommender.model.UserModel;
import io.github.uxlabspk.airecommender.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public void fetchUserInformation() {
        authRepository.fetchUserInformation();
    }

    public void registerUser(Context context, Uri imagePath, String userName, String userEmail, String userPassword) {
        authRepository.registerUser(context, imagePath, userName, userEmail, userPassword);
    }

    public void loginUser(String userEmail, String userPassword) {
        authRepository.loginUser(userEmail, userPassword);
    }

    public void updateUser(Context context, Uri imagePath, String userName) {
        authRepository.updateUser(context, imagePath, userName);
    }

    public void logoutUser() {
        authRepository.logoutUser();
    }

    public void deleteUser() {
        authRepository.deleteUser();
    }

    public void resetPassword(String email) {
        authRepository.resetPasswordRequest(email);
    }

    public LiveData<FirebaseUser> getUserLiveData() {
        return authRepository.getUserLiveData();
    }

    public LiveData<String> getErrorLiveData() {
        return authRepository.getErrorLiveData();
    }

    public LiveData<String> getSuccessLiveData() {
        return authRepository.getSuccessLiveData();
    }

    public LiveData<UserModel> getUserModelLiveData() {
        return authRepository.getUserModelLiveData();
    }
}
