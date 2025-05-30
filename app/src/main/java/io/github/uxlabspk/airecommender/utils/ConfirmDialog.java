package io.github.uxlabspk.airecommender.utils;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import java.util.Objects;

import io.github.uxlabspk.airecommender.R;

public class ConfirmDialog {
    private final Dialog dialog;
    private TextView dialog_headline;
    private TextView dialog_body;
    private Button yes_btn;
    private Button no_btn;


    public ConfirmDialog(Context context) {
        dialog = new Dialog(context);
        dialog.setContentView(R.layout.confirm_dialog_layout);
        init();
        findViews();
    }

    private void init() {
        Objects.requireNonNull(dialog.getWindow()).setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setGravity(Gravity.CENTER_VERTICAL);
    }

    private void findViews() {
        dialog_headline = dialog.findViewById(R.id.dialog_headline);
        dialog_body = dialog.findViewById(R.id.dialog_body);
        yes_btn = dialog.findViewById(R.id.yes_btn);
        no_btn = dialog.findViewById(R.id.no_btn);
    }

    public void show() {
        dialog.show();
    }

    public void dismiss() {
        dialog.dismiss();
    }

    public void setCanceledOnTouchOutside(boolean value) {
        dialog.setCanceledOnTouchOutside(value);
    }

    public Button getYesButton() {
        return yes_btn;
    }

    public Button getNoButton() {
        return no_btn;
    }

    public void setYesButtonText(String text) {
        yes_btn.setText(text);
    }

    public void setNoButtonText(String text) {
        no_btn.setText(text);
    }

    public void setDialogTitle(String title) {
        dialog_headline.setText(title);
    }

    public void setDialogDescription(String description) {
        dialog_body.setText(description);
    }
}
