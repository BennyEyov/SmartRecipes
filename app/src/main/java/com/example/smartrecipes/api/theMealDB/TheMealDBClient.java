package com.example.smartrecipes.api.theMealDB;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class TheMealDBClient {

    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static TheMealDBApiService apiService;

    public static TheMealDBApiService getInstance() {
        if (apiService == null) {
            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            apiService = retrofit.create(TheMealDBApiService.class);
        }
        return apiService;
    }
}
