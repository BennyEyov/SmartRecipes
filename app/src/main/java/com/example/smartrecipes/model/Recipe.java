package com.example.smartrecipes.model;

import android.os.Parcel;
import android.os.Parcelable;

public class Recipe implements Parcelable {
    private String id;
    private String title;
    private String ingredients;
    private String instructions;
    private String difficulty;
    private String duration;
    private String imageUrl;
    private boolean isFavorite;

    // Constructor ללא פרמטרים נדרש על ידי Firebase
    public Recipe() {
    }

    public Recipe(String title, String ingredients, String instructions,
                  String difficulty, String duration, String imageUrl, boolean isFavorite) {
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.difficulty = difficulty;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
    }

    public Recipe(String id, String title, String ingredients, String instructions,
                  String difficulty, String duration, String imageUrl, boolean isFavorite) {
        this.id = id;
        this.title = title;
        this.ingredients = ingredients;
        this.instructions = instructions;
        this.difficulty = difficulty;
        this.duration = duration;
        this.imageUrl = imageUrl;
        this.isFavorite = isFavorite;
    }


    // Getters ו־Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getIngredients() { return ingredients; }
    public void setIngredients(String ingredients) { this.ingredients = ingredients; }

    public String getInstructions() { return instructions; }
    public void setInstructions(String instructions) { this.instructions = instructions; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }

    public boolean isFavorite() { return isFavorite; }
    public void setFavorite(boolean favorite) { isFavorite = favorite; }

    protected Recipe(Parcel in) {
        id = in.readString();
        title = in.readString();
        ingredients = in.readString();
        instructions = in.readString();
        difficulty = in.readString();
        duration = in.readString();
        imageUrl = in.readString();
        isFavorite = in.readByte() != 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(title);
        dest.writeString(ingredients);
        dest.writeString(instructions);
        dest.writeString(difficulty);
        dest.writeString(duration);
        dest.writeString(imageUrl);
        dest.writeByte((byte) (isFavorite ? 1 : 0));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<Recipe> CREATOR = new Creator<Recipe>() {
        @Override
        public Recipe createFromParcel(Parcel in) {
            return new Recipe(in);
        }

        @Override
        public Recipe[] newArray(int size) {
            return new Recipe[size];
        }
    };
}
