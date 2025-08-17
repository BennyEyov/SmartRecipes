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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.CenterCrop;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
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
    private boolean isInternet = false;

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
            isInternet = args.getBoolean("isInternet");

            if (currentRecipe != null) {
                titleTextView.setText(currentRecipe.getTitle());
                durationTextView.setText("Duration: " + currentRecipe.getDuration());
                difficultyTextView.setText("Difficulty: " + currentRecipe.getDifficulty());
                ingredientsTextView.setText(currentRecipe.getIngredients());
                instructionsTextView.setText(currentRecipe.getInstructions());

                Glide.with(this)
                        .load(currentRecipe.getImageUrl())
                        .placeholder(R.drawable.placeholder_image)
                        .transform(new CenterCrop(), new RoundedCorners(16)) // חיתוך עם פינות מעוגלות
                        .into(recipeImageView);
            }
        }

        return view;
    }



    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        toolbar = view.findViewById(R.id.recipeToolbar);
        toolbar.setTitle("Recipe Details");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle);

        if (isInternet) {
            toolbar.inflateMenu(R.menu.recipe_internet_toolbar_menu);

            toolbar.setOnMenuItemClickListener(item -> {
                if (item.getItemId() == R.id.action_add_recipe) {
                    addRecipeToUserDatabase();
                    return true;
                }
                return false;
            });

        } else {
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
                    openEditFragment();
                    return true;

                } else if (itemId == R.id.action_delete) {
                    deleteRecipe(currentRecipe.getId());
                    return true;
                }

                return false;
            });
        }
    }

    private void addRecipeToUserDatabase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRecipesRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(uid);

        // צור ID ייחודי
        String id = userRecipesRef.push().getKey();
        if (id != null) {
            currentRecipe.setId(id);
            currentRecipe.setFavorite(false);

            userRecipesRef.child(id).setValue(currentRecipe)
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(getContext(), "Recipe added to your list", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this).popBackStack();
                    })
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), "Failed to add recipe", Toast.LENGTH_SHORT).show());
        }
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
                    requireActivity().getOnBackPressedDispatcher().onBackPressed(); // חזרה אחורה אחרי מחיקה
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), "Failed to delete", Toast.LENGTH_SHORT).show());
    }

    private void openEditFragment() {
        Bundle bundle = new Bundle();
        bundle.putParcelable("recipe", currentRecipe);

        NavController navController = Navigation.findNavController(requireView());
        navController.navigate(R.id.editRecipeFragment, bundle);
    }

    private void updateFavoriteIcon(MenuItem item, boolean isFavorite) {
        item.setIcon(isFavorite ? R.drawable.ic_favorite : R.drawable.ic_favorite_border);
    }


}

