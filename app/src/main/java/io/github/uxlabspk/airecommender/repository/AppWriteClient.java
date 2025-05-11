package io.github.uxlabspk.airecommender.repository;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class AppWriteClient {

    private static final String BASE_URL = "https://fra.cloud.appwrite.io/v1"; // e.g. https://cloud.appwrite.io/v1
    private static final String PROJECT_ID = "6820c311002bbdf5ffcd";
    private static final String JWT_TOKEN = "standard_6bb58563d95ea9476c4e4df26a018451bc6abf6a76273763b26ed36199436211bc17b217871a14ec66026f85bc94f6a0b103d31d82abef663e6eafce20a983007f401dd4e4a2425d7d14394febdd897552c6c3c7bffddeb31baa7fe1d43b6701ba9afa172b63c6e4daf5e15643209ab0bde321e3d81498b2395ee0d66d0e537f"; // Use JWT, NOT API key on client
    private static final String CONTENT_TYPE = "application/json";

    // Example GET request to list documents
    public static String getDocuments(String collectionId) {
        try {
            URL url = new URL(BASE_URL + "/databases/default/collections/" + collectionId + "/documents");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            // Headers
            conn.setRequestProperty("X-Appwrite-Project", PROJECT_ID);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE);
            conn.setRequestProperty("X-Appwrite-Key", JWT_TOKEN);

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "GET failed: " + e.getMessage();
        }
    }

    // Example POST request to create a document
    public static String createDocument(String collectionId, String documentId, String jsonBody) {
        try {
            URL url = new URL(BASE_URL + "/databases/default/collections/" + collectionId + "/documents");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            // Headers
            conn.setRequestProperty("X-Appwrite-Project", PROJECT_ID);
            conn.setRequestProperty("Content-Type", CONTENT_TYPE);
            conn.setRequestProperty("X-Appwrite-Key", JWT_TOKEN);

            // Body
            String requestBody = "{ \"documentId\": \"" + documentId + "\", \"data\": " + jsonBody + " }";
            OutputStream os = conn.getOutputStream();
            os.write(requestBody.getBytes());
            os.flush();
            os.close();

            // Read response
            BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }

            reader.close();
            return response.toString();

        } catch (Exception e) {
            e.printStackTrace();
            return "POST failed: " + e.getMessage();
        }
    }
}
