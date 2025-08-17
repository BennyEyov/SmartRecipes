package com.example.smartrecipes.api.theMealDB;

import retrofit2.Call;
import retrofit2.http.GET;

import com.example.smartrecipes.api.theMealDB.model.TheMealDBResponse;

public interface TheMealDBApiService {
    @GET("random.php")
    Call<TheMealDBResponse> getRandomMeal();
}
