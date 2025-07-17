package com.example.smartrecipes.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private RecyclerView recyclerView;
    private RecipeAdapter recipeAdapter;
    private List<Recipe> recipeList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        recyclerView = view.findViewById(R.id.recipesRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recipeList = new ArrayList<>();
        recipeAdapter = new RecipeAdapter(recipeList);
        recyclerView.setAdapter(recipeAdapter);

        loadRecipesFromFirebase();

        insertDefaultRecipesIfNeeded();

        return view;
    }

    private void loadRecipesFromFirebase() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference recipesRef = FirebaseDatabase.getInstance().getReference("recipes").child(uid);

        recipesRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                recipeList.clear();
                for (DataSnapshot recipeSnapshot : snapshot.getChildren()) {
                    Recipe recipe = recipeSnapshot.getValue(Recipe.class);
                    if (recipe != null) {
                        recipeList.add(recipe);
                    }
                }
                recipeAdapter.updateData(recipeList);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to load recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void insertDefaultRecipesIfNeeded() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRecipesRef = FirebaseDatabase.getInstance().getReference("recipes").child(uid);

        userRecipesRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // אין מתכונים - נכניס ברירת מחדל
                    List<Recipe> defaultRecipes = getDefaultRecipes();
                    for (Recipe recipe : defaultRecipes) {
                        String recipeId = userRecipesRef.push().getKey();
                        recipe.setId(recipeId);
                        userRecipesRef.child(recipeId).setValue(recipe);
                    }
                    Toast.makeText(getContext(), "Welcome! Added default recipes.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Failed to check default recipes", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private List<Recipe> getDefaultRecipes() {
        List<Recipe> list = new ArrayList<>();

        list.add(new Recipe("Pasta Bolognese",
                "Pasta, Tomato, Beef",
                "Cook beef, add tomato, mix with pasta",
                "Medium", "00:30",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Simple Salad",
                "Lettuce, Tomato, Cucumber",
                "Chop and mix all",
                "Easy", "00:10",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Omelette",
                "Eggs, Cheese",
                "Whisk and cook in pan",
                "Easy", "00:07",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Pancakes",
                "Flour, Milk, Eggs",
                "Mix and fry",
                "Easy", "00:20",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Chicken Curry",
                "Chicken, Curry Paste, Coconut Milk",
                "Cook all together",
                "Hard", "01:00",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Grilled Cheese",
                "Bread, Cheese",
                "Grill until melted",
                "Easy", "00:05",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Fruit Smoothie",
                "Banana, Berries, Yogurt",
                "Blend everything",
                "Easy", "00:05",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Baked Potatoes",
                "Potatoes, Salt",
                "Bake in oven",
                "Medium", "00:45",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        return list;
    }

}
