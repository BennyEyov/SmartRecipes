package com.example.smartrecipes.api.model;

import com.example.smartrecipes.model.Recipe;

import java.util.Random;

public class Meal {
    private String idMeal;
    private String strMeal;
    private String strInstructions;
    private String strMealThumb;

    private String strIngredient1;
    private String strIngredient2;
    private String strIngredient3;
    private String strIngredient4;
    private String strIngredient5;
    private String strIngredient6;
    private String strIngredient7;
    private String strIngredient8;
    private String strIngredient9;
    private String strIngredient10;
    private String strIngredient11;
    private String strIngredient12;
    private String strIngredient13;
    private String strIngredient14;
    private String strIngredient15;
    private String strIngredient16;
    private String strIngredient17;
    private String strIngredient18;
    private String strIngredient19;
    private String strIngredient20;

    private String strMeasure1;
    private String strMeasure2;
    private String strMeasure3;
    private String strMeasure4;
    private String strMeasure5;
    private String strMeasure6;
    private String strMeasure7;
    private String strMeasure8;
    private String strMeasure9;
    private String strMeasure10;
    private String strMeasure11;
    private String strMeasure12;
    private String strMeasure13;
    private String strMeasure14;
    private String strMeasure15;
    private String strMeasure16;
    private String strMeasure17;
    private String strMeasure18;
    private String strMeasure19;
    private String strMeasure20;


    // Getters and Setters
    public String getIdMeal() {
        return idMeal;
    }

    public void setIdMeal(String idMeal) {
        this.idMeal = idMeal;
    }

    public String getStrMeal() {
        return strMeal;
    }

    public void setStrMeal(String strMeal) {
        this.strMeal = strMeal;
    }

    public String getStrInstructions() {
        return strInstructions;
    }

    public void setStrInstructions(String strInstructions) {
        this.strInstructions = strInstructions;
    }

    public String getStrMealThumb() {
        return strMealThumb;
    }

    public void setStrMealThumb(String strMealThumb) {
        this.strMealThumb = strMealThumb;
    }

    public String getStrIngredient1() { return strIngredient1; }
    public String getStrIngredient2() { return strIngredient2; }
    public String getStrIngredient3() { return strIngredient3; }
    public String getStrIngredient4() { return strIngredient4; }
    public String getStrIngredient5() { return strIngredient5; }
    public String getStrIngredient6() { return strIngredient6; }
    public String getStrIngredient7() { return strIngredient7; }
    public String getStrIngredient8() { return strIngredient8; }
    public String getStrIngredient9() { return strIngredient9; }
    public String getStrIngredient10() { return strIngredient10; }
    public String getStrIngredient11() { return strIngredient1; }
    public String getStrIngredient12() { return strIngredient2; }
    public String getStrIngredient13() { return strIngredient3; }
    public String getStrIngredient14() { return strIngredient4; }
    public String getStrIngredient15() { return strIngredient5; }
    public String getStrIngredient16() { return strIngredient6; }
    public String getStrIngredient17() { return strIngredient7; }
    public String getStrIngredient18() { return strIngredient8; }
    public String getStrIngredient19() { return strIngredient9; }
    public String getStrIngredient20() { return strIngredient10; }

    public String getStrMeasure1() { return strMeasure1; }
    public String getStrMeasure2() { return strMeasure2; }
    public String getStrMeasure3() { return strMeasure3; }
    public String getStrMeasure4() { return strMeasure4; }
    public String getStrMeasure5() { return strMeasure5; }
    public String getStrMeasure6() { return strMeasure6; }
    public String getStrMeasure7() { return strMeasure7; }
    public String getStrMeasure8() { return strMeasure8; }
    public String getStrMeasure9() { return strMeasure9; }
    public String getStrMeasure10() { return strMeasure10; }
    public String getStrMeasure11() { return strMeasure1; }
    public String getStrMeasure12() { return strMeasure2; }
    public String getStrMeasure13() { return strMeasure3; }
    public String getStrMeasure14() { return strMeasure4; }
    public String getStrMeasure15() { return strMeasure5; }
    public String getStrMeasure16() { return strMeasure6; }
    public String getStrMeasure17() { return strMeasure7; }
    public String getStrMeasure18() { return strMeasure8; }
    public String getStrMeasure19() { return strMeasure9; }
    public String getStrMeasure20() { return strMeasure10; }

    /**
     * ממיר את המנה מה־API לאובייקט מסוג Recipe
     */
    public Recipe toRecipe() {
        // בניית מחרוזת מרכיבים + כמויות
        StringBuilder ingredientsBuilder = new StringBuilder();

        String[] ingredients = {
                strIngredient1, strIngredient2, strIngredient3, strIngredient4, strIngredient5,
                strIngredient6, strIngredient7, strIngredient8, strIngredient9, strIngredient10,
                strIngredient11, strIngredient12, strIngredient13, strIngredient14, strIngredient15,
                strIngredient16, strIngredient17, strIngredient18, strIngredient19, strIngredient20
        };

        String[] measures = {
                strMeasure1, strMeasure2, strMeasure3, strMeasure4, strMeasure5,
                strMeasure6, strMeasure7, strMeasure8, strMeasure9, strMeasure10,
                strMeasure11, strMeasure12, strMeasure13, strMeasure14, strMeasure15,
                strMeasure16, strMeasure17, strMeasure18, strMeasure19, strMeasure20
        };

        for (int i = 0; i < ingredients.length; i++) {
            String ingredient = ingredients[i];
            String measure = measures[i];

            if (ingredient != null && !ingredient.trim().isEmpty()) {
                ingredientsBuilder.append("• ");
                if (measure != null && !measure.trim().isEmpty()) {
                    ingredientsBuilder.append(measure.trim()).append(" ");
                }
                ingredientsBuilder.append(ingredient.trim()).append("\n");
            }
        }

        // רמות קושי אפשריות
        String[] difficulties = {"Easy", "Medium", "Hard"};
        // זמנים אפשריים בדקות
        String[] durations = {"00:15", "00:30", "00:45", "01:00"};

        // בחירה רנדומלית
        String randomDifficulty = difficulties[new Random().nextInt(difficulties.length)];
        String randomDuration = durations[new Random().nextInt(durations.length)];

        return new Recipe(
                strMeal,
                ingredientsBuilder.toString().trim(),
                strInstructions != null ? strInstructions.trim() : "",
                randomDifficulty,
                randomDuration,
                strMealThumb != null ? strMealThumb : "",
                false // isFavorite
        );
    }
}
