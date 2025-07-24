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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.example.smartrecipes.ui.home.RecipeAdapter;
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

    private List<Recipe> originalList = new ArrayList<>(); // שומר את כל המתכונים המקוריים
    private String currentQuery = "";
    private String currentDifficultyFilter = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_favorites, container, false);

        Toolbar toolbar = view.findViewById(R.id.favoritesToolbar);
        toolbar.setTitle("Favorite Recipes");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);
        setHasOptionsMenu(true); // מאפשר לתפריט להיטען

        recyclerView = view.findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(recipeAdapter);

        loadRecipesFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        // איפוס פילטרים
        currentDifficultyFilter = "";
        currentQuery = "";
        applyFilters();
    }


    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        inflater.inflate(R.menu.favorite_toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

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

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.action_filter) {
            showDifficultyFilterDialog();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void filterRecipes(String query) {
//        List<Recipe> filteredList = new ArrayList<>();
//        for (Recipe recipe : recipeList) {
//            if (recipe.getTitle().toLowerCase().contains(query.toLowerCase()) ||
//                    recipe.getIngredients().toLowerCase().contains(query.toLowerCase())) {
//                filteredList.add(recipe);
//            }
//        }
//        recipeAdapter.updateData(filteredList);
        currentQuery = query;
        applyFilters();
    }

    private void showDifficultyFilterDialog() {
        String[] difficulties = {"All", "Easy", "Medium", "Hard"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Difficulty")
                .setItems(difficulties, (dialog, which) -> {
                    if (which == 0) {
                        currentDifficultyFilter = ""; // איפוס
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
