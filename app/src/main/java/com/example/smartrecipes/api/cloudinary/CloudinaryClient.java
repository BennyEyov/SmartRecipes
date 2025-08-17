package com.example.smartrecipes.api.cloudinary;

import retrofit2.Retrofit;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class CloudinaryClient {

    private static final String BASE_URL = "https://api.cloudinary.com/";
    private static Retrofit retrofit;

    public static CloudinaryService getService() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(ScalarsConverterFactory.create())
                    .build();
        }
        return retrofit.create(CloudinaryService.class);
    }
}
