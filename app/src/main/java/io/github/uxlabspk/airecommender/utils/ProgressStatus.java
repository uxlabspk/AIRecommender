package io.github.uxlabspk.airecommender.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.Objects;

import io.github.uxlabspk.airecommender.R;

public class ProgressStatus {
    private Dialog dialog;
    private final Context context;
    private ImageView loadingIcon;
    private TextView loadingText;

    public ProgressStatus(Context context) {
        this.context = context;
        init();
        findViews();
    }

    private void init() {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.progress_status_layout);
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER);
    }

    private void findViews() {
        loadingIcon = dialog.findViewById(R.id.loadingIcon);
        loadingText = dialog.findViewById(R.id.loadingText);
    }

    public void setTitle(String title) {
        loadingText.setText(title);
    }

    public void show() {
        Animation refresh_anim = AnimationUtils.loadAnimation(context, R.anim.refresh_anim);
        loadingIcon.setAnimation(refresh_anim);

        dialog.show();
    }

    public void dismiss() {
        loadingIcon.clearAnimation();
        dialog.dismiss();
    }

    public void setCanceledOnTouchOutside(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
    }
}
