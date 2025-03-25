package io.github.uxlabspk.airecommender.view.fragments;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.util.Objects;

import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.databinding.FragmentProfileBinding;
import io.github.uxlabspk.airecommender.utils.ConfirmDialog;
import io.github.uxlabspk.airecommender.utils.ProgressStatus;
import io.github.uxlabspk.airecommender.view.AccountActivity;
import io.github.uxlabspk.airecommender.view.EditProfile;
import io.github.uxlabspk.airecommender.view.IntroductionActivity;
import io.github.uxlabspk.airecommender.view.LoginActivity;
import io.github.uxlabspk.airecommender.view.MainActivity;
import io.github.uxlabspk.airecommender.viewmodel.AuthViewModel;

public class ProfileFragment extends Fragment {
    private AuthViewModel authViewModel;
    private FragmentProfileBinding binding;

    public ProfileFragment() {}

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        init();
        return binding.getRoot();
    }

    private void init() {
        // initializing the viewmodel
        authViewModel = new ViewModelProvider(this).get(AuthViewModel.class);

        // fetch user information
        authViewModel.fetchUserInformation();

        // observe the changes
        observeChanges();

        // on edit profile
        binding.editProfile.setOnClickListener(view -> {
            startActivity(new Intent(getContext(), EditProfile.class));
        });

        // on logout clicked
        binding.logoutBtn.setOnClickListener(v -> {
            // Ready the progress status
            ProgressStatus progressStatus = new ProgressStatus(getContext());
            progressStatus.setCanceledOnTouchOutside(true);
            progressStatus.setTitle("logging out...");
            progressStatus.show();

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                authViewModel.logoutUser();
                startActivity(new Intent(getContext(), AccountActivity.class));
                requireActivity().finish();
                progressStatus.dismiss();
            }, 3000);
        });

        // delete the user
        binding.deleteBtn.setOnClickListener(v -> {
            // Ready the dialog
            ConfirmDialog confirmDialog = new ConfirmDialog(getContext());
            confirmDialog.setCanceledOnTouchOutside(false);
            confirmDialog.setDialogTitle("Confirm to Delete");
            confirmDialog.setDialogDescription("Are you sure to delete your account and all data?");
            confirmDialog.setYesButtonText("Delete");
            confirmDialog.setNoButtonText("Cancel");

            // on dialog delete button clicked
            confirmDialog.getYesButton().setOnClickListener(v1 -> {
                authViewModel.deleteUser();
                startActivity(new Intent(getContext(), AccountActivity.class));
                requireActivity().finish();
                confirmDialog.dismiss();
            });

            // on dialog cancel button clicked
            confirmDialog.getNoButton().setOnClickListener(v2 -> {
                confirmDialog.dismiss();
            });

            confirmDialog.show();
        });
    }

    private void observeChanges() {
        authViewModel.getUserModelLiveData().observe(getViewLifecycleOwner(), user -> {
            binding.userName.setText(user.getUserName());
            binding.userEmail.setText(user.getUserEmail());
            if (user.getUserAvatar().isEmpty()) binding.userProfilePic.setImageResource(R.drawable.ic_user);
            else Glide.with(getContext()).load(user.getUserAvatar()).into(binding.userProfilePic);
        });

        // getting error data
        authViewModel.getErrorLiveData().observe(getViewLifecycleOwner(), error -> {
            if (error != null) Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
        });

        // getting success data
        authViewModel.getSuccessLiveData().observe(getViewLifecycleOwner(), successMessage -> {
            if (successMessage != null) {
                Toast.makeText(getContext(), successMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }

}