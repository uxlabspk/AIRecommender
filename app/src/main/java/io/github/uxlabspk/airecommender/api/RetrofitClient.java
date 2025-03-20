package io.github.uxlabspk.airecommender.api;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {
    private static final String BASE_URL = "https://api-inference.huggingface.co/";

    private static final Retrofit retrofit = new Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build();

    public static HuggingFaceApi getApiService() {
        return retrofit.create(HuggingFaceApi.class);
    }
}
