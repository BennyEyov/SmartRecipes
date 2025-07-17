package com.example.smartrecipes.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;

import java.io.IOException;
import java.util.UUID;

public class AddRecipeFragment extends Fragment {

    private EditText titleEditText, ingredientsEditText, instructionsEditText;
    private Spinner difficultySpinner;
    private NumberPicker hourPicker;

    private NumberPicker minutePicker;

    private ImageView recipeImageView;
    private Button saveBtn;

    private Uri imageUri;

    private static final int PICK_IMAGE_REQUEST = 1;

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
        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);
        recipeImageView  = view.findViewById(R.id.recipeImageView);
        saveBtn = view.findViewById(R.id.saveRecipeButton);

        recipeImageView.setOnClickListener(v -> openImagePicker());
        saveBtn.setOnClickListener(v -> saveRecipe());

        return view;
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            imageUri = data.getData();
            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(
                        requireActivity().getContentResolver(), imageUri);
                recipeImageView.setImageBitmap(bitmap);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void saveRecipe() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        String title = titleEditText.getText().toString();
        String ingredients = ingredientsEditText.getText().toString();
        String instructions = instructionsEditText.getText().toString();
        String difficulty = difficultySpinner.getSelectedItem().toString();
        String duration = String.format("%02d:%02d",
                hourPicker.getValue(), minutePicker.getValue());

        if (imageUri != null) {
            String fileName = "recipes/" + UUID.randomUUID() + ".jpg";

            FirebaseStorage.getInstance().getReference(fileName)
                    .putFile(imageUri)
                    .addOnSuccessListener(taskSnapshot -> taskSnapshot.getStorage().getDownloadUrl()
                            .addOnSuccessListener(uri -> {
                                Recipe recipe = new Recipe(
                                        title, ingredients, instructions,
                                        difficulty, duration,
                                        uri.toString(), false
                                );
                                FirebaseDatabase.getInstance()
                                        .getReference("recipes")
                                        .child(uid)
                                        .push()
                                        .setValue(recipe);
                                Toast.makeText(getContext(), "Recipe saved", Toast.LENGTH_SHORT).show();
                            }))
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Image upload failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        } else {
            Toast.makeText(getContext(), "Please select an image", Toast.LENGTH_SHORT).show();
        }

    }
}
