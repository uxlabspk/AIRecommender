package io.github.uxlabspk.airecommender.view.Adapters;

import  android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Objects;

import io.github.uxlabspk.airecommender.R;
import io.github.uxlabspk.airecommender.api.SupabaseImageUploader;
import io.github.uxlabspk.airecommender.model.ImageModel;

public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.ViewHolder> {
    private final Context context;
    private final List<ImageModel> imageList;

    public ImageAdapter(Context context, List<ImageModel> imageList) {
        this.context = context;
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_image, parent,false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        // loading the image
        Glide.with(context).load(imageList.get(position).getImageUrl()).into(holder.imageView);

        // on download button click
        holder.downloadBtn.setOnClickListener(view -> downloadAndSaveImageFromUrl(context, imageList.get(position).getImageUrl()));

        // delete the image
        holder.deleteBtn.setOnClickListener(view -> {
           deleteImage(context, imageList.get(position).getImageUrl());
           imageList.remove(position);
           notifyItemRemoved(position);
        });
    }

    public void downloadAndSaveImageFromUrl(Context context, String imageUrl) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            InputStream inputStream = null;

            try {
                // Download the image
                URL url = new URL(imageUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.connect();

                if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> Toast.makeText(context, "Failed to download image", Toast.LENGTH_SHORT).show());
                    return;
                }

                inputStream = new BufferedInputStream(connection.getInputStream());
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);

                if (bitmap == null) {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> Toast.makeText(context, "Failed to process image", Toast.LENGTH_SHORT).show());
                    return;
                }

                // Extract filename from URL
                String path = url.getPath();
                String fileName = path.substring(path.lastIndexOf('/') + 1);

                // If filename doesn't have an extension, add .jpg
                if (!fileName.contains(".")) {
                    fileName += ".jpg";
                }

                // Save to gallery
                ContentValues values = new ContentValues();
                values.put(MediaStore.Images.Media.DISPLAY_NAME, fileName);
                values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
                values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

                ContentResolver resolver = context.getContentResolver();
                Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

                if (imageUri != null) {
                    OutputStream outputStream = resolver.openOutputStream(imageUri);
                    if (outputStream != null) {
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                        outputStream.close();
                        android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                        mainHandler.post(() -> Toast.makeText(context, "Image Saved To Gallery", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                    mainHandler.post(() -> Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show());
                }

            } catch (Exception e) {
                e.printStackTrace();
                android.os.Handler mainHandler = new android.os.Handler(context.getMainLooper());
                mainHandler.post(() -> Toast.makeText(context, "Error : " + e.getMessage(), Toast.LENGTH_SHORT).show());
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }

    private void deleteImage(Context context, String imagePath) {
        SupabaseImageUploader imageUploader = new SupabaseImageUploader(context, "img");

        String fileName = imagePath.substring(imagePath.lastIndexOf("/") + 1);

        // If the image is in a subfolder, include the subfolder path
        String pathWithFileName = fileName;

        // Delete the image
        imageUploader.deleteImage(pathWithFileName, new SupabaseImageUploader.DeleteCallback() {
            @Override
            public void onSuccess(String message) {
                // Remove from the list and update UI
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Successfully deleted", Toast.LENGTH_SHORT).show();
                });

            }

            @Override
            public void onFailure(String errorMessage) {
                new Handler(Looper.getMainLooper()).post(() -> {
                    Toast.makeText(context, "Failed to delete: " + errorMessage, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return imageList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageView;
        ImageView downloadBtn;
        ImageView deleteBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.image_item);
            downloadBtn = itemView.findViewById(R.id.downloadBtn);
            deleteBtn = itemView.findViewById(R.id.deleteBtn);
        }
    }
}
