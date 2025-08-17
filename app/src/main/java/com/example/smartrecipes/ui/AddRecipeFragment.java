package com.example.smartrecipes.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.example.smartrecipes.api.cloudinary.CloudinaryClient;
import com.example.smartrecipes.api.cloudinary.CloudinaryService;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AddRecipeFragment extends Fragment {

    private EditText titleEditText, ingredientsEditText, instructionsEditText;
    private Spinner difficultySpinner;
    private NumberPicker hourPicker, minutePicker;
    private ImageView recipeImageView;
    private Button saveBtn;
    private Uri imageUri;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    imageUri = result.getData().getData();
                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                                requireActivity().getContentResolver(), imageUri);
                        recipeImageView.setImageBitmap(bitmap);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            });


    public AddRecipeFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_add_recipe, container, false);

        titleEditText = view.findViewById(R.id.titleEditText);
        ingredientsEditText = view.findViewById(R.id.ingredientsEditText);
        instructionsEditText = view.findViewById(R.id.instructionsEditText);
        difficultySpinner = view.findViewById(R.id.difficultySpinner);
        hourPicker = view.findViewById(R.id.hourPicker);
        minutePicker = view.findViewById(R.id.minutePicker);
        recipeImageView  = view.findViewById(R.id.recipeImageView);
        saveBtn = view.findViewById(R.id.saveRecipeButton);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        recipeImageView.setOnClickListener(v -> openImagePicker());
        saveBtn.setOnClickListener(v -> saveRecipe());

        return view;
    }

    private void resetFields() {
        titleEditText.setText("");
        ingredientsEditText.setText("");
        instructionsEditText.setText("");
        difficultySpinner.setSelection(0);
        hourPicker.setValue(0);
        minutePicker.setValue(0);
        recipeImageView.setImageResource(R.drawable.placeholder_image); // ודא שקובץ כזה קיים בתיקיית drawable
        imageUri = null;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }


    private void saveRecipe() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String title = titleEditText.getText().toString();
        String ingredients = ingredientsEditText.getText().toString();
        String instructions = instructionsEditText.getText().toString();
        String difficulty = difficultySpinner.getSelectedItem().toString();
        String duration = String.format("%02d:%02d", hourPicker.getValue(), minutePicker.getValue());

        if (imageUri != null) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(imageUri);
                byte[] imageBytes = new byte[inputStream.available()];
                inputStream.read(imageBytes);
                inputStream.close();

                RequestBody requestFile = RequestBody.create(okhttp3.MediaType.parse("image/*"), imageBytes);
                MultipartBody.Part body = MultipartBody.Part.createFormData("file", "image.jpg", requestFile);
                RequestBody uploadPreset = RequestBody.create(okhttp3.MultipartBody.FORM, "smartrecipes_preset");

                CloudinaryService service = CloudinaryClient.getService();
                Call<ResponseBody> call = service.uploadImage(body, uploadPreset);

                call.enqueue(new Callback<ResponseBody>() {
                    @Override
                    public void onResponse(@NonNull Call<ResponseBody> call, @NonNull Response<ResponseBody> response) {
                        if (response.isSuccessful()) {
                            try {
                                String responseBody = response.body().string();
                                JSONObject json = new JSONObject(responseBody);
                                String imageUrl = json.getString("secure_url");

                                DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                                        .getReference("recipes")
                                        .child(uid)
                                        .push(); // יוצר ID ייחודי

                                String recipeId = recipeRef.getKey();

                                Recipe recipe = new Recipe(
                                        title, ingredients, instructions,
                                        difficulty, duration,
                                        imageUrl, false
                                );

                                recipe.setId(recipeId);

                                recipeRef.setValue(recipe)
                                        .addOnSuccessListener(unused -> {
                                            Toast.makeText(getContext(), "Recipe saved", Toast.LENGTH_SHORT).show();
                                            resetFields();
                                            BottomNavigationView bottomNav = requireActivity().findViewById(R.id.bottom_navigation);
                                            bottomNav.setSelectedItemId(R.id.homeFragment);

                                        });

                            } catch (Exception e) {
                                e.printStackTrace();
                                Toast.makeText(getContext(), "Upload succeeded, but error parsing response", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(@NonNull Call<ResponseBody> call, @NonNull Throwable t) {
                        Toast.makeText(getContext(), "Upload error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
                Toast.makeText(getContext(), "Error reading image file", Toast.LENGTH_SHORT).show();
            }
        } else {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }
    }
}
