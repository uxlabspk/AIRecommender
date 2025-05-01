package io.github.uxlabspk.airecommender.api;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;

public class ImageRequest {

    private static final String TAG = "ImageRequest";

    public interface ImageCallback {
        void onSuccess(Bitmap image);
        void onError(Exception e);
    }

    public static void fetchImageWithPost(String requestUrl, String prompt, String authToken, ImageCallback callback) {
        new Thread(() -> {
            HttpURLConnection connection = null;
            try {
                // Create the JSON payload
                JSONObject jsonPayload = new JSONObject();
                jsonPayload.put("prompt", prompt);
                String jsonInputString = jsonPayload.toString();

                Log.d(TAG, "Sending request to: " + requestUrl);
                Log.d(TAG, "Request payload: " + jsonInputString);

                // Setup connection
                URL url = new URL(requestUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setDoOutput(true);

                // Set required headers
                connection.setRequestProperty("Authorization", authToken);
                connection.setRequestProperty("Content-Type", "application/json");

                // Write JSON data
                try (OutputStream os = connection.getOutputStream()) {
                    byte[] input = jsonInputString.getBytes(StandardCharsets.UTF_8);
                    os.write(input, 0, input.length);
                }

                // Check response code
                int responseCode = connection.getResponseCode();
                Log.d(TAG, "Response code: " + responseCode);

                if (responseCode >= 200 && responseCode <= 299) {
                    // Read the response as a string
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder responseBuilder = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        responseBuilder.append(line);
                    }
                    reader.close();

                    String responseString = responseBuilder.toString();
                    Log.d(TAG, "Response: " + responseString);

                    // Parse JSON response
                    JSONObject jsonResponse = new JSONObject(responseString);
                    String imageUrl = null;

                    // Try to extract image URL
                    if (jsonResponse.has("images")) {
                        JSONArray imagesArray = jsonResponse.getJSONArray("images");
                        if (imagesArray.length() > 0) {
                            JSONObject imageObj = imagesArray.getJSONObject(0);
                            if (imageObj.has("url")) {
                                imageUrl = imageObj.getString("url");
                            }
                        }
                    }

                    if (imageUrl != null) {
                        Log.d(TAG, "Fetching image from URL: " + imageUrl);
                        URL imgUrl = new URL(imageUrl);
                        HttpURLConnection imgConnection = (HttpURLConnection) imgUrl.openConnection();
                        imgConnection.connect();
                        InputStream imgStream = imgConnection.getInputStream();
                        Bitmap bitmap = BitmapFactory.decodeStream(imgStream);
                        imgStream.close();
                        imgConnection.disconnect();

                        if (bitmap != null) {
                            Log.d(TAG, "Bitmap fetched from URL successfully");
                            callback.onSuccess(bitmap);
                        } else {
                            throw new Exception("Failed to decode bitmap from URL");
                        }
                    } else {
                        throw new Exception("No image URL found in response");
                    }
                } else {
                    InputStream errorStream = connection.getErrorStream();
                    StringBuilder errorMessage = new StringBuilder();
                    if (errorStream != null) {
                        BufferedReader reader = new BufferedReader(new InputStreamReader(errorStream));
                        String line;
                        while ((line = reader.readLine()) != null) {
                            errorMessage.append(line);
                        }
                        reader.close();
                    }
                    throw new Exception("API error: " + responseCode + " - " + errorMessage.toString());
                }

            } catch (Exception e) {
                Log.e(TAG, "Error: " + e.getMessage(), e);
                if (callback != null) {
                    callback.onError(e);
                }
            } finally {
                if (connection != null) {
                    connection.disconnect();
                }
            }
        }).start();
    }
}
