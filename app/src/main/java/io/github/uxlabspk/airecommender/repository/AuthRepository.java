package io.github.uxlabspk.airecommender.repository;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.Objects;

import io.github.uxlabspk.airecommender.model.UserModel;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;

    // Constructor
    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
    }

    // SignIn With Email and Password
    public void registerUser(String name, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                       if (firebaseUser != null) {
                           UserModel userModel = new UserModel(firebaseUser.getUid(), name, email);
                           databaseReference.child(firebaseUser.getUid()).setValue(userModel);
                           userLiveData.setValue(firebaseUser);
                       }
                   } else {
                       errorLiveData.setValue(Objects.requireNonNull(task.getException()).getMessage());
                   }
                });
    }

    // Login with Email and Password
    public void loginUser(String email, String password) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       userLiveData.setValue(firebaseAuth.getCurrentUser());
                   } else {
                       errorLiveData.setValue(Objects.requireNonNull(task.getException()).getMessage());
                   }
                });
    }

    // Continue with google
    public void continueWithGoogle(AuthCredential authCredential) {
        firebaseAuth.signInWithCredential(authCredential)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                       if (firebaseUser != null) {
                           databaseReference.child(firebaseUser.getUid()).setValue(new UserModel(firebaseUser.getUid(), firebaseUser.getDisplayName(), firebaseUser.getEmail()));
                           userLiveData.setValue(firebaseUser);
                       }
                   } else {
                       errorLiveData.setValue(Objects.requireNonNull(task.getException()).getMessage());
                   }
                });
    }

    // Getter
    public LiveData<FirebaseUser> getUserLiveData() {
        return  userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

}
