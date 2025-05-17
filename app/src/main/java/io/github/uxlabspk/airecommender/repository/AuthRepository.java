package io.github.uxlabspk.airecommender.repository;

import android.content.Context;
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

import io.github.uxlabspk.airecommender.api.SupabaseImageUploader;
import io.github.uxlabspk.airecommender.model.UserModel;

public class AuthRepository {
    private final FirebaseAuth firebaseAuth;
    private final DatabaseReference databaseReference;
    private final MutableLiveData<FirebaseUser> userLiveData;
    private final MutableLiveData<String> errorLiveData;
    private final MutableLiveData<String> successLiveData;
    private final MutableLiveData<UserModel> userModelLiveData;

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
    public void registerUser(Context context, Uri imagePath, String name, String email, String password) {
        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                assert firebaseUser != null;

                if (imagePath != null) {
                    SupabaseImageUploader uploader = new SupabaseImageUploader(
                            context,
                            "img"
                    );

                    // Upload an image from gallery (after you've selected an image and have its Uri)
                    uploader.uploadImage(imagePath, "",  new SupabaseImageUploader.UploadCallback() {
                        @Override
                        public void onSuccess(String fileUrl) {
                            Log.d("ImageUpload", "Success! URL: " + fileUrl);
                            UserModel userModel = new UserModel(firebaseUser.getUid(), fileUrl, name, email);
                            databaseReference.child("Users").child(firebaseUser.getUid()).setValue(userModel);
                            userLiveData.setValue(firebaseUser);
                        }

                        @Override
                        public void onFailure(String errorMessage) {
                            // Handle failed upload
                            errorLiveData.setValue("Failed to upload an image");
                            Log.e("ImageUpload", "Failed: " + errorMessage);
                        }
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
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                userLiveData.setValue(firebaseAuth.getCurrentUser());
            } else {
                errorLiveData.setValue(Objects.requireNonNull(task.getException()).getMessage());
            }
        });
    }

    // Continue with google
    public void continueWithGoogle(AuthCredential authCredential) {
        firebaseAuth.signInWithCredential(authCredential).addOnCompleteListener(task -> {
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
        firebaseAuth.sendPasswordResetEmail(email).addOnCompleteListener(resetTask -> {
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

        databaseReference.child("Users").child(firebaseAuth.getCurrentUser().getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
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

    // update user information
    public void updateUser(Context context, Uri imagePath, String userName) {
        if (imagePath == null) {
            databaseReference.child("Users").child(FirebaseAuth.getInstance().getUid()).child("userName").setValue(userName)
                            .addOnSuccessListener(task -> {
                                successLiveData.setValue("Profile updated successfully");
                            })
                            .addOnFailureListener(error -> {
                                errorLiveData.setValue("Failed to update profile: " + error.getMessage());

                            });
            return;
        }

        SupabaseImageUploader uploader = new SupabaseImageUploader(
                context,
                "img"
        );

        // Upload the new image
        uploader.uploadImage(imagePath, "", new SupabaseImageUploader.UploadCallback() {
            @Override
            public void onSuccess(String fileUrl) {
                Log.d("ImageUpload", "Update success! URL: " + fileUrl);

                // Update only the profile image URL in the database
                databaseReference.child("Users").child(FirebaseAuth.getInstance().getUid()).child("userName").setValue(userName);
                databaseReference.child("Users").child(FirebaseAuth.getInstance().getUid()).child("userAvatar").setValue(fileUrl)
                        .addOnSuccessListener(aVoid -> {
                            // Notify UI about successful update
                            successLiveData.setValue("Profile image updated successfully");
                        })
                        .addOnFailureListener(e -> {
                            errorLiveData.setValue("Failed to update profile: " + e.getMessage());
                            Log.e("ProfileUpdate", "Failed: " + e.getMessage());
                        });
            }

            @Override
            public void onFailure(String errorMessage) {
                errorLiveData.setValue("Failed to upload new profile image");
                Log.e("ImageUpload", "Failed: " + errorMessage);
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
                Log.d("TAG", "No image found or failed to delete image: " + Objects.requireNonNull(task.getException()).getMessage());
            }

            // Step 2: Delete the user's data from Realtime Database
            databaseReference.child(Objects.requireNonNull(firebaseAuth.getUid())).removeValue().addOnCompleteListener(dbTask -> {
                if (dbTask.isSuccessful()) {
                    Log.d("TAG", "User database entry deleted successfully");
                } else {
                    Log.d("TAG", "Failed to delete user database entry: " + Objects.requireNonNull(dbTask.getException()).getMessage());
                }

                // Step 3: Delete the user from Firebase Authentication
                Objects.requireNonNull(firebaseAuth.getCurrentUser()).delete().addOnCompleteListener(authTask -> {
                    if (authTask.isSuccessful()) {
                        Log.d("TAG", "User deleted successfully from Authentication");
                    } else {
                        Log.d("TAG", "Failed to delete user: " + Objects.requireNonNull(authTask.getException()).getMessage());
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
        return userLiveData;
    }

    public LiveData<String> getErrorLiveData() {
        return errorLiveData;
    }

    public LiveData<String> getSuccessLiveData() {
        return successLiveData;
    }

    public LiveData<UserModel> getUserModelLiveData() {
        return userModelLiveData;
    }
}
