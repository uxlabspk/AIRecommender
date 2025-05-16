package io.github.uxlabspk.airecommender.repository;

import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;
import android.webkit.MimeTypeMap;

import androidx.annotation.NonNull;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * A utility class for uploading images to Supabase storage.
 */
public class SupabaseImageUploader {
    private static final String TAG = "SupabaseImageUploader";

    // Replace these with your actual Supabase details
    private final String SUPABASE_URL;
    private final String SUPABASE_API_KEY;
    private final String BUCKET_NAME;

    private final OkHttpClient client;
    private final Context context;

    /**
     * Constructor for SupabaseImageUploader
     *
     * @param context Application context
     * @param supabaseUrl Your Supabase project URL
     * @param supabaseApiKey Your Supabase API key
     * @param bucketName Storage bucket name
     */
    public SupabaseImageUploader(Context context, String supabaseUrl, String supabaseApiKey, String bucketName) {
        this.context = context;
        this.SUPABASE_URL = supabaseUrl;
        this.SUPABASE_API_KEY = supabaseApiKey;
        this.BUCKET_NAME = bucketName;

        // Set up OkHttpClient with reasonable timeouts
        this.client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .build();
    }

    /**
     * Interface for handling upload responses
     */
    public interface UploadCallback {
        void onSuccess(String fileUrl);
        void onFailure(String errorMessage);
    }

    /**
     * Upload an image from Uri to Supabase storage
     *
     * @param imageUri Uri of the image to upload
     * @param callback Callback to handle the result
     */
    public void uploadImage(Uri imageUri, UploadCallback callback) {
        try {
            // Generate a unique file name
            String fileName = UUID.randomUUID().toString();

            // Get file extension from Uri
            String fileExtension = getFileExtension(imageUri);
            if (fileExtension != null) {
                fileName += "." + fileExtension;
            } else {
                fileName += ".jpg"; // Default extension
            }

            // Get input stream from Uri
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                callback.onFailure("Failed to read image from Uri");
                return;
            }

            // Convert to byte array
            byte[] imageData = getBytesFromInputStream(inputStream);

            // Get MIME type
            String mimeType = getMimeType(imageUri);
            if (mimeType == null) {
                mimeType = "image/jpeg"; // Default MIME type
            }

            // Perform the upload
            uploadImageData(imageData, fileName, mimeType, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing image upload: " + e.getMessage());
            callback.onFailure("Error preparing image: " + e.getMessage());
        }
    }

    /**
     * Upload a bitmap image to Supabase storage
     *
     * @param bitmap Bitmap to upload
     * @param format Bitmap compression format
     * @param quality Compression quality (0-100)
     * @param callback Callback to handle the result
     */
    public void uploadBitmap(Bitmap bitmap, Bitmap.CompressFormat format, int quality, UploadCallback callback) {
        try {
            // Generate a unique file name with appropriate extension
            String extension = format == Bitmap.CompressFormat.PNG ? "png" :
                    (format == Bitmap.CompressFormat.WEBP ? "webp" : "jpg");
            String fileName = UUID.randomUUID().toString() + "." + extension;

            // Convert bitmap to byte array
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(format, quality, baos);
            byte[] imageData = baos.toByteArray();

            // Get MIME type
            String mimeType = "image/" + extension;

            // Perform the upload
            uploadImageData(imageData, fileName, mimeType, callback);

        } catch (Exception e) {
            Log.e(TAG, "Error preparing bitmap upload: " + e.getMessage());
            callback.onFailure("Error preparing bitmap: " + e.getMessage());
        }
    }

    /**
     * Upload image from a file path to Supabase storage
     *
     * @param filePath Path to the image file
     * @param callback Callback to handle the result
     */
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

    /**
     * Core method to upload image data to Supabase
     *
     * @param imageData Image as byte array
     * @param fileName Target file name in storage
     * @param mimeType MIME type of the image
     * @param callback Callback to handle the result
     */
    private void uploadImageData(byte[] imageData, String fileName, String mimeType, UploadCallback callback) {
        // Create request body with the image data
        RequestBody requestBody = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("file", fileName,
                        RequestBody.create(MediaType.parse(mimeType), imageData))
                .build();

        // Create POST request to Supabase Storage API
        String uploadUrl = SUPABASE_URL + "/storage/v1/object/" + BUCKET_NAME + "/" + fileName;
        Request request = new Request.Builder()
                .url(uploadUrl)
                .addHeader("apikey", SUPABASE_API_KEY)
                .addHeader("Authorization", "Bearer " + SUPABASE_API_KEY)
                .post(requestBody)
                .build();

        // Execute the request asynchronously
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
                    // Response is typically JSON with metadata about the uploaded file
                    if (response.body() != null) {
                        String responseBody = response.body().string();
                        Log.d(TAG, "Upload success: " + responseBody);

                        // Construct the public URL for the image
                        String fileUrl = SUPABASE_URL + "/storage/v1/object/public/" + BUCKET_NAME + "/" + fileName;
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

    /**
     * Utility method to get file extension from Uri
     */
    private String getFileExtension(Uri uri) {
        String extension = null;
        try {
            String mimeType = context.getContentResolver().getType(uri);
            if (mimeType != null) {
                extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(mimeType);
            } else {
                // Try to get from path
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

    /**
     * Utility method to get MIME type from Uri
     */
    private String getMimeType(Uri uri) {
        String mimeType = context.getContentResolver().getType(uri);
        if (mimeType == null) {
            // Try to determine from extension
            String fileExtension = getFileExtension(uri);
            if (fileExtension != null) {
                mimeType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(fileExtension.toLowerCase());
            }
        }
        return mimeType;
    }

    /**
     * Utility method to get MIME type from file name
     */
    private String getMimeTypeFromFileName(String fileName) {
        String extension = fileName.substring(fileName.lastIndexOf(".") + 1).toLowerCase();
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    /**
     * Utility method to read bytes from an input stream
     */
    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }

    /**
     * Utility method to read bytes from a file
     */
    private byte[] getBytesFromFile(File file) throws IOException {
        byte[] bytes = new byte[(int) file.length()];
        try (FileInputStream fis = new FileInputStream(file)) {
            fis.read(bytes);
        }
        return bytes;
    }
}