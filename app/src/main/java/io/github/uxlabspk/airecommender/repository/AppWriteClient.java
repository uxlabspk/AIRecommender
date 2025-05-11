package io.github.uxlabspk.airecommender.repository;


import android.content.Context;

import io.appwrite.Client;
import io.appwrite.services.Storage;

public class AppWriteClient {

    public AppWriteClient() {

    }

    public Storage initializeStorage(Context context) {
        Client client = new Client(context)
                .setEndpoint("https://fra.cloud.appwrite.io/v1")
                .setProject("6820c311002bbdf5ffcd");

        return new Storage(client);
    }

}
