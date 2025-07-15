package com.example.smartrecipes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RecipeFragment extends Fragment {

    private ImageView recipeImageView;
    private TextView titleTextView, durationTextView, difficultyTextView,
            ingredientsTextView, instructionsTextView;

    private Recipe currentRecipe;

    private MaterialToolbar toolbar;
    private boolean isEditMode = false;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_recipe, container, false);

        recipeImageView = view.findViewById(R.id.recipeImageView);
        titleTextView = view.findViewById(R.id.recipeTitle);
        durationTextView = view.findViewById(R.id.recipeDuration);
        difficultyTextView = view.findViewById(R.id.recipeDifficulty);
        ingredientsTextView = view.findViewById(R.id.ingredientsText);
        instructionsTextView = view.findViewById(R.id.instructionsText);

        Bundle args = getArguments();
        if (args != null && args.containsKey("recipe")) {
            currentRecipe = args.getParcelable("recipe");

            if (currentRecipe != null) {
                titleTextView.setText(currentRecipe.getTitle());
                durationTextView.setText("Duration: " + currentRecipe.getDuration());
                difficultyTextView.setText("Difficulty: " + currentRecipe.getDifficulty());
                ingredientsTextView.setText(currentRecipe.getIngredients());
                instructionsTextView.setText(currentRecipe.getInstructions());

                Glide.with(this)
                        .load(currentRecipe.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .into(recipeImageView);
            }
        }

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.recipeToolbar);
        toolbar.inflateMenu(R.menu.recipe_toolbar_menu);

        updateFavoriteIcon(toolbar.getMenu().findItem(R.id.action_toggle_favorite),
                currentRecipe.isFavorite());

        toolbar.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();

            if (itemId == R.id.action_toggle_favorite) {
                currentRecipe.setFavorite(!currentRecipe.isFavorite());
                updateFavoriteIcon(item, currentRecipe.isFavorite());
                updateRecipeInFirebase(currentRecipe);
                return true;

            } else if (itemId == R.id.action_edit) {
                enableEditMode();
                return true;

            } else if (itemId == R.id.action_delete) {
                deleteRecipe(currentRecipe.getId());
                return true;

            } else if (itemId == R.id.action_save) {
                saveEditedRecipe();
                return true;
            }

            return false;

        });
    }


    private void updateRecipeInFirebase(Recipe recipe) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(uid)
                .child(recipe.getId());

        recipeRef.setValue(recipe)
                .addOnSuccessListener(aVoid ->
                        Toast.makeText(getContext(), "Recipe updated", Toast.LENGTH_SHORT).show())
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to update", Toast.LENGTH_SHORT).show());
    }

    private void deleteRecipe(String id) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recipeRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(uid)
                .child(id);

        recipeRef.removeValue()
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(getContext(), "Recipe deleted", Toast.LENGTH_SHORT).show();
                    requireActivity().onBackPressed(); // חזרה אחורה אחרי מחיקה
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    private void enableEditMode() {
        isEditMode = true;

        titleTextView.setEnabled(true);
        ingredientsTextView.setEnabled(true);
        instructionsTextView.setEnabled(true);

        titleTextView.setBackgroundResource(android.R.drawable.editbox_background);
        ingredientsTextView.setBackgroundResource(android.R.drawable.editbox_background);
        instructionsTextView.setBackgroundResource(android.R.drawable.editbox_background);

        // מחליפים את התפריט לתפריט עם "שמור"
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.recipe_edit_menu);

        Toast.makeText(getContext(), "Edit mode enabled", Toast.LENGTH_SHORT).show();
    }

    private void saveEditedRecipe() {
        isEditMode = false;

        currentRecipe.setTitle(titleTextView.getText().toString());
        currentRecipe.setIngredients(ingredientsTextView.getText().toString());
        currentRecipe.setInstructions(instructionsTextView.getText().toString());

        updateRecipeInFirebase(currentRecipe);

        // מחזירים את השדות למצב רגיל
        titleTextView.setEnabled(false);
        ingredientsTextView.setEnabled(false);
        instructionsTextView.setEnabled(false);

        titleTextView.setBackgroundResource(0);
        ingredientsTextView.setBackgroundResource(0);
        instructionsTextView.setBackgroundResource(0);

        // מחזירים את התפריט המקורי
        toolbar.getMenu().clear();
        toolbar.inflateMenu(R.menu.recipe_toolbar_menu);

        // מעדכנים את אייקון הלב בהתאם למצב נוכחי
        MenuItem favItem = toolbar.getMenu().findItem(R.id.action_toggle_favorite);
        updateFavoriteIcon(favItem, currentRecipe.isFavorite());

        Toast.makeText(getContext(), "Recipe saved", Toast.LENGTH_SHORT).show();
    }


    private void updateFavoriteIcon(MenuItem item, boolean isFavorite) {
        item.setIcon(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }

}

