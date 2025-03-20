package io.github.uxlabspk.airecommender.api;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Header;
import retrofit2.http.POST;

public interface HuggingFaceApi {
    @POST("models/strangerzonehf/Flux-Midjourney-Mix2-LoRA") // API endpoint
    Call<ResponseBody> generateImage(
            @Header("Authorization") String apiKey,
            @Body RequestBody requestBody
    );
}

