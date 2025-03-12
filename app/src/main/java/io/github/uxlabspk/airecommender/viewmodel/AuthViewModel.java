package io.github.uxlabspk.airecommender.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseUser;

import io.github.uxlabspk.airecommender.repository.AuthRepository;

public class AuthViewModel extends ViewModel {
    private final AuthRepository authRepository;

    public AuthViewModel() {
        authRepository = new AuthRepository();
    }

    public void registerUser(String userName, String userEmail, String userPassword) {
        authRepository.registerUser(userName, userEmail, userPassword);
    }

    public void loginUser(String userEmail, String userPassword) {
        authRepository.loginUser(userEmail, userPassword);
    }

    public void continueWithGoogle(AuthCredential authCredential) {
        authRepository.continueWithGoogle(authCredential);
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
}
