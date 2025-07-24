package com.example.smartrecipes.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import androidx.navigation.Navigation;

import com.bumptech.glide.Glide;
import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;

import java.util.List;

import android.widget.ImageView;

public class RecipeAdapter extends RecyclerView.Adapter<RecipeAdapter.RecipeViewHolder> {

    private List<Recipe> recipeList;

    private boolean isInternet = false;

    public RecipeAdapter(List<Recipe> recipeList) {
        this.recipeList = recipeList;
    }

    // קונסטרקטור עבור מתכונים מהאינטרנט
    public RecipeAdapter(List<Recipe> recipeList, boolean isInternet) {
        this.recipeList = recipeList;
        this.isInternet = isInternet;
    }

    @NonNull
    @Override
    public RecipeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe, parent, false);
        return new RecipeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecipeViewHolder holder, int position) {
        Recipe recipe = recipeList.get(position);
        holder.titleTextView.setText(recipe.getTitle());
        holder.durationTextView.setText("Duration: " + recipe.getDuration());
        holder.difficultyTextView.setText("Difficulty: " + recipe.getDifficulty());

        if (recipe.getImageUrl() != null && !recipe.getImageUrl().isEmpty()) {
            Glide.with(holder.itemView.getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.placeholder_image)
                    .into(holder.recipeImageView);
        } else {
            holder.recipeImageView.setImageResource(R.drawable.placeholder_image);
        }

        holder.itemView.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putParcelable("recipe", recipe);
            if (isInternet){
                bundle.putBoolean("isInternet", true);
            }
            Navigation.findNavController(v).navigate(R.id.recipeFragment, bundle);

        });
    }

    @Override
    public int getItemCount() {
        return recipeList.size();
    }

    public void updateData(List<Recipe> newRecipes) {
        this.recipeList = newRecipes;
        notifyDataSetChanged();
    }

    static class RecipeViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, durationTextView, difficultyTextView;
        ImageView recipeImageView;

        public RecipeViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.titleTextView);
            durationTextView = itemView.findViewById(R.id.durationTextView);
            difficultyTextView = itemView.findViewById(R.id.difficultyTextView);
            recipeImageView = itemView.findViewById(R.id.recipeImageView);
        }
    }
}