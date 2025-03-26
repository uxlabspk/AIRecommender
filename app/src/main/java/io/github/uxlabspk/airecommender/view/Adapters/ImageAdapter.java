package io.github.uxlabspk.airecommender.view.Adapters;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
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

import java.io.File;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import io.github.uxlabspk.airecommender.R;
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
        holder.downloadBtn.setOnClickListener(view -> {
            saveImageToGalleryModern(imageList.get(position).getImageUrl());
        });

        // delete the image
        holder.deleteBtn.setOnClickListener(view -> {
           deleteImage(imageList.get(position).getImageUrl());
           imageList.remove(position);
           notifyItemRemoved(position);
        });
    }

    private void saveImageToGalleryModern(String imagePath) {
        try {
            // Create a file object from the image path
            File sourceFile = new File(imagePath);
            if (!sourceFile.exists()) {
                Toast.makeText(context, "Source image doesn't exist", Toast.LENGTH_SHORT).show();
                return;
            }

            // Get bitmap from file
            Bitmap bitmap = BitmapFactory.decodeFile(sourceFile.getAbsolutePath());

            // Create values for the new image
            ContentValues values = new ContentValues();
            values.put(MediaStore.Images.Media.DISPLAY_NAME, sourceFile.getName());
            values.put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg");
            values.put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES);

            // Insert the image
            ContentResolver resolver = context.getContentResolver();
            Uri imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

            if (imageUri != null) {
                // Open output stream and save bitmap
                OutputStream outputStream = resolver.openOutputStream(imageUri);
                if (outputStream != null) {
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
                    outputStream.close();

                    // Notify user
                    Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void deleteImage(String imagePath) {
        // Extracting the file name from path
        String fileName = "";
        try {
            File file = new File(new URI(imagePath).getPath());
            fileName = file.getName();
        } catch (URISyntaxException error) {
            Log.d("ERROR", "deleteImage: " + error.getMessage());
        }

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("images/" + FirebaseAuth.getInstance().getCurrentUser().getUid() + "/" +  fileName);

        storageRef.delete().addOnCompleteListener(task -> {
           if (task.isComplete()) {
               Toast.makeText(context, "Image deleted successfully", Toast.LENGTH_SHORT).show();
           } else {
               Toast.makeText(context, "Failed to delete image", Toast.LENGTH_SHORT).show();
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
