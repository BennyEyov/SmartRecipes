package com.example.smartrecipes.ui;

import android.os.Bundle;
import android.os.Handler;
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
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipes.R;
import com.example.smartrecipes.adapter.RecipeAdapter;
import com.example.smartrecipes.api.theMealDB.TheMealDBApiService;
import com.example.smartrecipes.api.theMealDB.TheMealDBClient;
import com.example.smartrecipes.api.theMealDB.model.Meal;
import com.example.smartrecipes.api.theMealDB.model.TheMealDBResponse;
import com.example.smartrecipes.model.Recipe;
import com.example.smartrecipes.viewmodel.InternetRecipesViewModel;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InternetRecipesFragment extends Fragment implements MenuProvider {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private InternetRecipesViewModel viewModel;

    private static final long MIN_REFRESH_INTERVAL_MS = 5000;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_internet_recipes, container, false);

        // ViewModel
        viewModel = new ViewModelProvider(this).get(InternetRecipesViewModel.class);

        // Toolbar
        Toolbar toolbar = view.findViewById(R.id.internetToolbar);
        toolbar.setTitle("Internet Recipes");
        toolbar.setTitleTextColor(ContextCompat.getColor(requireContext(), R.color.black));
        toolbar.setTitleTextAppearance(requireContext(), R.style.ToolbarTitleStyle);
        ((AppCompatActivity) requireActivity()).setSupportActionBar(toolbar);

        // הוספת MenuProvider
        requireActivity().addMenuProvider(this, getViewLifecycleOwner(), Lifecycle.State.RESUMED);

        // RecyclerView
        recyclerView = view.findViewById(R.id.internetRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeAdapter = new RecipeAdapter(viewModel.recipeList, true);
        recyclerView.setAdapter(recipeAdapter);

        // טען רק אם ריק
        if (viewModel.originalList.isEmpty()) {
            fetchRecipes();
        } else {
            applyFilters();
        }

        return view;
    }

    private void fetchRecipes() {
        if (viewModel.isRefreshing) return;

        viewModel.isRefreshing = true;
        viewModel.recipeList.clear();
        viewModel.originalList.clear();
        recipeAdapter.updateData(viewModel.recipeList);
        viewModel.successfulMeals = 0;

        TheMealDBApiService service = TheMealDBClient.getInstance();

        for (int i = 0; i < 10; i++) {
            service.getRandomMeal().enqueue(new Callback<TheMealDBResponse>() {
                @Override
                public void onResponse(@NonNull Call<TheMealDBResponse> call, @NonNull Response<TheMealDBResponse> response) {
                    TheMealDBResponse theMealDBResponse = response.body();

                    if (theMealDBResponse != null && theMealDBResponse.getMeals() != null && !theMealDBResponse.getMeals().isEmpty()) {
                        Meal meal = theMealDBResponse.getMeals().get(0);

                        if (meal.getStrMeal() != null && meal.getStrInstructions() != null) {
                            Recipe recipe = meal.toRecipe();
                            viewModel.recipeList.add(recipe);
                            viewModel.originalList.add(recipe);
                            viewModel.successfulMeals++;

                            if (viewModel.successfulMeals == 10) {
                                viewModel.isRefreshing = false;
                                applyFilters();
                            }
                        }
                    }
                }

                @Override
                public void onFailure(@NonNull Call<TheMealDBResponse> call, @NonNull Throwable t) {

                }
            });
        }

        new Handler().postDelayed(() -> {
            if (viewModel.successfulMeals < 10) {
                viewModel.isRefreshing = false;
                Toast.makeText(requireContext(), "Could not fetch enough recipes. Try again.", Toast.LENGTH_SHORT).show();
                applyFilters(); // הצג את מה שיש
            }
        }, 5000);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.currentQuery = "";
        viewModel.currentDifficultyFilter = "";
        applyFilters();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menuInflater.inflate(R.menu.internet_toolbar_menu, menu);

        MenuItem searchItem = menu.findItem(R.id.action_search);
        SearchView searchView = (SearchView) searchItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.currentQuery = query;
                applyFilters();
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.currentQuery = newText;
                applyFilters();
                return true;
            }
        });
    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        int id = menuItem.getItemId();

        if (id == R.id.action_filter) {
            showDifficultyFilterDialog();
            return true;
        }

        if (id == R.id.action_refresh) {
            long now = System.currentTimeMillis();
            if (now - viewModel.lastRefreshTime < MIN_REFRESH_INTERVAL_MS) {
                Toast.makeText(getContext(), "Please wait before refreshing again", Toast.LENGTH_SHORT).show();
            } else {
                viewModel.lastRefreshTime = now;
                fetchRecipes();
            }
            return true;
        }

        return false;
    }

    private void showDifficultyFilterDialog() {
        String[] difficulties = {"All", "Easy", "Medium", "Hard"};

        new AlertDialog.Builder(requireContext())
                .setTitle("Filter by Difficulty")
                .setItems(difficulties, (dialog, which) -> {
                    if (which == 0) {
                        viewModel.currentDifficultyFilter = "";
                    } else {
                        viewModel.currentDifficultyFilter = difficulties[which];
                    }
                    applyFilters();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void applyFilters() {
        List<Recipe> filtered = new ArrayList<>();

        for (Recipe recipe : viewModel.originalList) {
            boolean matchesQuery = viewModel.currentQuery.isEmpty() ||
                    recipe.getTitle().toLowerCase().contains(viewModel.currentQuery.toLowerCase()) ||
                    recipe.getIngredients().toLowerCase().contains(viewModel.currentQuery.toLowerCase());

            boolean matchesDifficulty = viewModel.currentDifficultyFilter.isEmpty() ||
                    recipe.getDifficulty().equalsIgnoreCase(viewModel.currentDifficultyFilter);

            if (matchesQuery && matchesDifficulty) {
                filtered.add(recipe);
            }
        }

        recipeAdapter.updateData(filtered);
    }
}
