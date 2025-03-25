package io.github.uxlabspk.airecommender.repository;

import android.net.Uri;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

import io.github.uxlabspk.airecommender.model.UserModel;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<String> successLiveData;
    private final MutableLiveData<UserModel> userModelLiveData;

    private String downlaodURL;

    // Constructor
    public AuthRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference();
        userLiveData = new MutableLiveData<>();
        errorLiveData = new MutableLiveData<>();
        successLiveData = new MutableLiveData<>();
        userModelLiveData = new MutableLiveData<>();
    }

    // SignUp With Email and Password
    public void registerUser(Uri imagePath, String name, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                   if (task.isSuccessful()) {
                       FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                       assert firebaseUser != null;

                       if (imagePath != null) {
                           StorageReference ref = FirebaseStorage.getInstance().getReference().child("images/" + firebaseAuth.getCurrentUser().getUid());
                           ref.putFile(imagePath).addOnSuccessListener(taskSnapshot -> {
                               ref.getDownloadUrl().addOnSuccessListener(uri -> {
                                   downlaodURL = uri.toString();
                                   Log.d("TAG", "registerUser: " + downlaodURL);
                                   UserModel userModel = new UserModel(firebaseUser.getUid(), uri.toString(), name, email);
                                   databaseReference.child("Users").child(firebaseUser.getUid()).setValue(userModel);
                                   userLiveData.setValue(firebaseUser);
                               });
                           }).addOnFailureListener(failure -> {
                               errorLiveData.setValue(failure.getMessage());
                           });
                       } else {
                           UserModel userModel = new UserModel(firebaseUser.getUid(), "", name, email);
                           databaseReference.child("Users").child(firebaseUser.getUid()).setValue(userModel);
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
                           databaseReference.child(firebaseUser.getUid()).setValue(new UserModel(firebaseUser.getUid(), Objects.requireNonNull(firebaseUser.getPhotoUrl()).toString(), firebaseUser.getDisplayName(), firebaseUser.getEmail()));
                           userLiveData.setValue(firebaseUser);
                       }
                   } else {
                       errorLiveData.setValue(Objects.requireNonNull(task.getException()).getMessage());
                   }
                });
    }

    // reset password
    public void resetPasswordRequest(String email) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(resetTask -> {
                    if (resetTask.isSuccessful()) {
                        successLiveData.setValue("Reset link sent to your email.");
                    } else {
                        errorLiveData.setValue(Objects.requireNonNull(resetTask.getException()).getMessage());
                    }
                });
    }

    // fetch user Information
    public void fetchUserInformation() {
        if (firebaseAuth.getCurrentUser() == null) {
            errorLiveData.setValue("No user is signed in");
            return;
        }

        databaseReference.child("Users").child(firebaseAuth.getCurrentUser().getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        UserModel userModel = snapshot.getValue(UserModel.class);
                        if (userModel != null) {
                            userModelLiveData.setValue(userModel);
                        } else {
                            errorLiveData.setValue("User data not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        errorLiveData.setValue(error.getMessage());
                    }
                });
    }

    // logout user
    public void logoutUser() {
        firebaseAuth.signOut();
    }

    // delete user
    public void deleteUser() {

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Users");
        StorageReference storageReference = FirebaseStorage.getInstance().getReference().child("images/" + firebaseAuth.getUid());

        // Step 1: Delete the user's image from Firebase Storage
        storageReference.delete().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                Log.d("TAG", "User image deleted successfully");
            } else {
                Log.d("TAG", "No image found or failed to delete image: " + task.getException().getMessage());
            }

            // Step 2: Delete the user's data from Realtime Database
            databaseReference.child(firebaseAuth.getUid()).removeValue().addOnCompleteListener(dbTask -> {
                if (dbTask.isSuccessful()) {
                    Log.d("TAG", "User database entry deleted successfully");
                } else {
                    Log.d("TAG", "Failed to delete user database entry: " + dbTask.getException().getMessage());
                }

                // Step 3: Delete the user from Firebase Authentication
                firebaseAuth.getCurrentUser().delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Log.d("TAG", "User deleted successfully from Authentication");
                    } else {
                        Log.d("TAG", "Failed to delete user: " + authTask.getException().getMessage());
                        if (authTask.getException() != null) {
                            Log.d("TAG", "User needs to re-authenticate before deleting.");
                        }
                    }
                });
            });
        });
    }

    // Getter
    public LiveData<FirebaseUser> getUserLiveData() {
        return  userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<String>  getSuccessLiveData() {
        return successLiveData;
    }

    public LiveData<UserModel> getUserModelLiveData() {
        return userModelLiveData;
    }

}
