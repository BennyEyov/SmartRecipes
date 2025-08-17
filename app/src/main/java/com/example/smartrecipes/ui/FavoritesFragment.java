package com.example.smartrecipes.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipes.R;
import com.example.smartrecipes.adapter.RecipeAdapter;
import com.example.smartrecipes.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavoritesFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;
    private List<Recipe> originalList = new ArrayList<>();

    private String currentQuery = "";
    private String currentDifficultyFilter = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        // שיוך ה-toolbar העליון
        Toolbar toolbar = view.findViewById(R.id.favoritesToolbar);
        toolbar.setTitle("Favorite Recipes");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        recyclerView = view.findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(recipeAdapter);

        // הוספת תגובתיות ל-toolbar
        requireActivity().addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.favorite_toolbar_menu, menu);

                MenuItem searchItem = menu.findItem(R.id.action_search);
                SearchView searchView = (SearchView) searchItem.getActionView();

                // דואגים לפונקציונליות של כפתור החיפוש
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        filterRecipes(query);
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        filterRecipes(newText);
                        return true;
                    }
                });
            }

            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                if (menuItem.getItemId() == R.id.action_filter) {
                    showDifficultyFilterDialog();
                    return true;
                }
                return false;
            }
        }, getViewLifecycleOwner());

        loadRecipesFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        currentDifficultyFilter = "";
        currentQuery = "";
        applyFilters();
    }

    private void filterRecipes(String query) {
        currentQuery = query;
        applyFilters();
    }

    private void showDifficultyFilterDialog() {
        String[] difficulties = {"All", "Easy", "Medium", "Hard"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Difficulty")
                .setItems(difficulties, (dialog, which) -> {
                    if (which == 0) {
                        currentDifficultyFilter = "";
                    } else {
                        currentDifficultyFilter = difficulties[which];
                    }
                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loadRecipesFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes").child(uid);

        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                originalList.clear();

                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null && recipe.isFavorite()) {
                        recipeList.add(recipe);
                        originalList.add(recipe);
                    }
                }
                applyFilters();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load favorites", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilters() {
        List<Recipe> filteredList = new ArrayList<>();

        for (Recipe recipe : originalList) {
            boolean matchesQuery = currentQuery.isEmpty() ||
                    recipe.getTitle().toLowerCase().contains(currentQuery.toLowerCase()) ||
                    recipe.getIngredients().toLowerCase().contains(currentQuery.toLowerCase());

            boolean matchesDifficulty = currentDifficultyFilter.isEmpty() ||
                    recipe.getDifficulty().equalsIgnoreCase(currentDifficultyFilter);

            if (matchesQuery && matchesDifficulty) {
                filteredList.add(recipe);
            }
        }

        recipeAdapter.updateData(filteredList);
    }
}
