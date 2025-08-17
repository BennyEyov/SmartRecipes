package com.example.smartrecipes.ui;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.*;
import android.widget.*;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.*;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.example.smartrecipes.api.cloudinary.CloudinaryClient;
import com.example.smartrecipes.api.cloudinary.CloudinaryService;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;

import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditRecipeFragment extends Fragment {

    private EditText titleEditText, ingredientsEditText, instructionsEditText;
    private Spinner difficultySpinner;
    private NumberPicker hourPicker, minutePicker;
    private ImageView imageView;
    private MaterialToolbar toolbar;

    private Uri newImageUri = null;

    private Recipe currentRecipe;

    private final ActivityResultLauncher<Intent> imagePickerLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                    newImageUri = result.getData().getData();
                    imageView.setImageURI(newImageUri); // הצגת התמונה החדשה שנבחרה
                }
            });

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_edit_recipe, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view,
                              @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.editRecipeToolbar);
        toolbar.setTitle("Edit Recipe");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle);

        titleEditText = view.findViewById(R.id.editRecipeTitle);
        ingredientsEditText = view.findViewById(R.id.editRecipeIngredients);
        instructionsEditText = view.findViewById(R.id.editRecipeInstructions);
        difficultySpinner = view.findViewById(R.id.editDifficultySpinner);
        hourPicker = view.findViewById(R.id.editHourPicker);
        minutePicker = view.findViewById(R.id.editMinutePicker);
        imageView = view.findViewById(R.id.editRecipeImageView);

        hourPicker.setMinValue(0);
        hourPicker.setMaxValue(23);
        minutePicker.setMinValue(0);
        minutePicker.setMaxValue(59);

        imageView.setOnClickListener(v -> openImagePicker());

        // טען נתוני מתכון
        Bundle args = getArguments();
        if (args != null && args.containsKey("recipe")) {
            currentRecipe = args.getParcelable("recipe");

            if (currentRecipe != null) {
                titleEditText.setText(currentRecipe.getTitle());
                ingredientsEditText.setText(currentRecipe.getIngredients());
                instructionsEditText.setText(currentRecipe.getInstructions());

                // טען Difficulty ל-Spinner
                String[] difficulties = getResources().getStringArray(R.array.difficulty_levels);
                for (int i = 0; i < difficulties.length; i++) {
                    if (difficulties[i].equals(currentRecipe.getDifficulty())) {
                        difficultySpinner.setSelection(i);
                        break;
                    }
                }

                // טען זמן ל-NumberPickers
                String[] timeParts = currentRecipe.getDuration().split(":");
                if (timeParts.length == 2) {
                    hourPicker.setValue(Integer.parseInt(timeParts[0].trim()));
                    minutePicker.setValue(Integer.parseInt(timeParts[1].trim()));
                }

                Glide.with(this)
                        .load(currentRecipe.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(imageView);
            }
        }

        toolbar.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.action_save) {
                saveChanges();
                return true;
            }
            return false;
        });
    }

    private void openImagePicker() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        imagePickerLauncher.launch(intent);
    }

    private void saveChanges() {
        currentRecipe.setTitle(titleEditText.getText().toString());
        currentRecipe.setIngredients(ingredientsEditText.getText().toString());
        currentRecipe.setInstructions(instructionsEditText.getText().toString());
        currentRecipe.setDifficulty(difficultySpinner.getSelectedItem().toString());

        String duration = String.format("%02d:%02d", hourPicker.getValue(), minutePicker.getValue());
        currentRecipe.setDuration(duration);

        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(uid)
                .child(currentRecipe.getId());

        // בדוק אם נבחרה תמונה חדשה
        if (newImageUri != null) {
            try {
                InputStream inputStream = requireContext().getContentResolver().openInputStream(newImageUri);
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

                                currentRecipe.setImageUrl(imageUrl);  // עדכן קישור התמונה

                                recipeRef.setValue(currentRecipe)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(getContext(), "Recipe updated", Toast.LENGTH_SHORT).show();
                                            requireActivity().getOnBackPressedDispatcher().onBackPressed();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
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
            // לא נבחרה תמונה חדשה – רק שמור את שאר הנתונים
            recipeRef.setValue(currentRecipe)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Recipe updated", Toast.LENGTH_SHORT).show();
                        requireActivity().getOnBackPressedDispatcher().onBackPressed();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(getContext(), "Update failed", Toast.LENGTH_SHORT).show();
                    });
        }
    }
}
