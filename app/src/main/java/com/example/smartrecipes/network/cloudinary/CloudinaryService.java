package com.example.smartrecipes.network.cloudinary;

import okhttp3.MultipartBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface CloudinaryService {

    @Multipart
    @POST("v1_1/dpfzw63t7/image/upload")
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part file,
            @Part("upload_preset") okhttp3.RequestBody uploadPreset
    );
}
