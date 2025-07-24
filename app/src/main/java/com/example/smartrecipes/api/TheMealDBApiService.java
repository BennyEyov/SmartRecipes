package com.example.smartrecipes.api;

import retrofit2.Call;
import retrofit2.http.GET;

import com.example.smartrecipes.api.model.TheMealDBResponse;

public interface TheMealDBApiService {
    @GET("random.php")
    Call<TheMealDBResponse> getRandomMeal();
}
