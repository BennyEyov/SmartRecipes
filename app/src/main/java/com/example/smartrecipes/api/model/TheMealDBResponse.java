package com.example.smartrecipes.api.model;

import com.example.smartrecipes.api.model.Meal;

import java.util.List;

public class TheMealDBResponse {
    private List<Meal> meals;

    public List<Meal> getMeals() {
        return meals;
    }

    public void setMeals(List<Meal> meals) {
        this.meals = meals;
    }
}
