package io.github.uxlabspk.airecommender.api;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import io.github.uxlabspk.airecommender.BuildConfig;
import io.github.uxlabspk.airecommender.model.ImageModel;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class SupabaseImageUploader {
    private static final String TAG = "SupabaseImageUploader";

    private final String SUPABASE_URL = "https://mwjvfrfjumjsubzganou.supabase.co";
    private final String SUPABASE_API_KEY = BuildConfig.supabaseApiKey;
    private final String BUCKET_NAME;
    private final OkHttpClient client;
    private final Context context;

    public SupabaseImageUploader(Context context, String bucketName) {
        this.context = context;
        this.BUCKET_NAME = bucketName;

        // Set up OkHttpClient with reasonable timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    public void uploadImage(Uri imageUri, String fileName, UploadCallback callback) {
        try {
            if (fileName.isBlank()) {
                fileName = UUID.randomUUID().toString();
            }

            String fileExtension = getFileExtension(imageUri);
            if (fileExtension != null) {
                fileName += "." + fileExtension;
            } else {
                fileName += ".jpg"; // Default extension
            }

            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure("Failed to read image from Uri");
                return;
            }

            byte[] imageData = getBytesFromInputStream(inputStream);

            String mimeType = getMimeType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // Default MIME type
            }

            uploadImageData(imageData, fileName, mimeType, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload: " + e.getMessage());
            callback.onFailure("Error preparing image: " + e.getMessage());
        }
    }

    public void uploadImageFile(String filePath, UploadCallback callback) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                callback.onFailure("File does not exist: " + filePath);
                return;
            }

            // Get file name and extension
            String fileName = file.getName();
            if (!fileName.contains(".")) {
                fileName = UUID.randomUUID().toString() + ".jpg";
            }

            // Get MIME type
            String mimeType = getMimeTypeFromFileName(fileName);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // Default MIME type
            }

            // Read file data
            byte[] imageData = getBytesFromFile(file);

            // Perform the upload
            uploadImageData(imageData, fileName, mimeType, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing file upload: " + e.getMessage());
            callback.onFailure("Error preparing file: " + e.getMessage());
        }
    }

    private void uploadImageData(byte[] imageData, String fileName, String mimeType, UploadCallback callback) {
        // Create request body with the image data
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(MediaType.parse(mimeType), imageData))
                .build();

        // Create POST request to Supabase Storage API
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + FirebaseAuth.getInstance().getUid() + "/" + fileName;
        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Upload failed: " + e.getMessage());
                callback.onFailure("Upload failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Error response: " + response.code() + " - " + errorBody);
                    callback.onFailure("Error: " + response.code() + " - " + errorBody);
                    return;
                }

                try {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Upload success: " + responseBody);

                        String fileUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + FirebaseAuth.getInstance().getUid() + "/" + fileName;
                        callback.onSuccess(fileUrl);
                    } else {
                        callback.onFailure("Empty response body");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    callback.onFailure("Error processing response: " + e.getMessage());
                }
            }
        });
    }

    private String getFileExtension(Uri uri) {
        String extension = null;
        try {
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            } else {
                String path = uri.getPath();
                if (path != null && path.contains(".")) {
                    extension = path.substring(path.lastIndexOf(".") + 1);
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting file extension: " + e.getMessage());
        }
        return extension;
    }

    private String getMimeType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            String fileExtension = getFileExtension(uri);
            if (fileExtension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        }
        return mimeType;
    }

    private String getMimeTypeFromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        return bytes;
    }

    public void listImagesInBucket(ListImagesCallback callback) {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        // Create the prefix path based on user ID
        String prefix = userId + "/generated/";

        // Create POST request to Supabase Storage API to list objects
        String listUrl = SUPABASE_URL + "/storage/v1/object/list/" + BUCKET_NAME;

        // Create the JSON request body
        JSONObject requestBody = new JSONObject();
        try {
            requestBody.put("prefix", prefix);
            requestBody.put("limit", 100);
        } catch (JSONException e) {
            callback.onFailure("Failed to create request: " + e.getMessage());
            return;
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                requestBody.toString()
        );

        Request request = new Request.Builder()
                .url(listUrl)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "List images failed: " + e.getMessage());
                callback.onFailure("Failed to list images: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Error response: " + response.code() + " - " + errorBody);
                    callback.onFailure("Error: " + response.code() + " - " + errorBody);
                    return;
                }

                try {
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "List images success: " + responseBody);

                        // Parse JSON and create image models
                        List<ImageModel> imageModels = parseImageListResponse(responseBody);
                        callback.onSuccess(imageModels);
                    } else {
                        callback.onFailure("Empty response body");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    callback.onFailure("Error processing response: " + e.getMessage());
                }
            }
        });
    }

    private List<ImageModel> parseImageListResponse(String responseBody) {
        List<ImageModel> imageModels = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(responseBody);

            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject imageObject = jsonArray.getJSONObject(i);
                String name = imageObject.getString("name");

                // Create the full URL for the image
                String imageUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + FirebaseAuth.getInstance().getUid() + "/generated/" + name;
                imageModels.add(new ImageModel(imageUrl));
            }
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing JSON response: " + e.getMessage());
        }

        return imageModels;
    }

    public void deleteImage(String fileName, DeleteCallback callback) {
        // Get the current user ID
        String userId = FirebaseAuth.getInstance().getUid();
        if (userId == null) {
            callback.onFailure("User not authenticated");
            return;
        }

        // Create DELETE request to Supabase Storage API
        String deleteUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + userId + "/generated/" + fileName;

        Request request = new Request.Builder()
                .url(deleteUrl)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .delete()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Delete failed: " + e.getMessage());
                callback.onFailure("Delete failed: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (!response.isSuccessful()) {
                    String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                    Log.e(TAG, "Error response: " + response.code() + " - " + errorBody);
                    callback.onFailure("Error: " + response.code() + " - " + errorBody);
                    return;
                }

                try {
                    Log.d(TAG, "Delete success for file: " + fileName);
                    callback.onSuccess("File deleted successfully: " + fileName);
                } catch (Exception e) {
                    Log.e(TAG, "Error processing response: " + e.getMessage());
                    callback.onFailure("Error processing response: " + e.getMessage());
                }
            }
        });
    }

    // Callback interface for delete operation
    public interface DeleteCallback {
        void onSuccess(String message);
        void onFailure(String errorMessage);
    }

    // Callback interface for listing images
    public interface ListImagesCallback {
        void onSuccess(List<ImageModel> images);
        void onFailure(String errorMessage);
    }

    public interface UploadCallback {
        void onSuccess(String fileUrl);
        void onFailure(String errorMessage);
    }
}