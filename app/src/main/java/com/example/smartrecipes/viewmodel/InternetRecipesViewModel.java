package com.example.smartrecipes.viewmodel;

import androidx.lifecycle.ViewModel;
import com.example.smartrecipes.model.Recipe;
import java.util.ArrayList;
import java.util.List;

public class InternetRecipesViewModel extends ViewModel {
    public List<Recipe> recipeList = new ArrayList<>();
    public List<Recipe> originalList = new ArrayList<>();

    public String currentQuery = "";
    public String currentDifficultyFilter = "";

    public boolean isRefreshing = false;
    public int successfulMeals = 0;
    public long lastRefreshTime = 0;
}
