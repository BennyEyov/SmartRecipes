package com.example.smartrecipes.ui;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.smartrecipes.R;
import com.example.smartrecipes.model.Recipe;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class RegisterFragment extends Fragment {

    private EditText emailEditText, passwordEditText;
    private Button registerButton;
    private TextView loginRedirectButton;
    private FirebaseAuth mAuth;

    public RegisterFragment() {
        // בשביל הפיירבייס
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);

        mAuth = FirebaseAuth.getInstance();

        emailEditText = view.findViewById(R.id.registerEmailEditText);
        passwordEditText = view.findViewById(R.id.registerPasswordEditText);
        registerButton = view.findViewById(R.id.registerButton);
        loginRedirectButton = view.findViewById(R.id.loginRedirectButton);

        registerButton.setOnClickListener(v -> registerUser());

        loginRedirectButton.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_registerFragment_to_loginFragment));

        return view;
    }

    private void registerUser() {
        String email = emailEditText.getText().toString().trim();
        String password = passwordEditText.getText().toString().trim();

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getContext(), "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 6) {
            Toast.makeText(getContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show();
            return;
        }

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(getContext(), "Registration successful!", Toast.LENGTH_SHORT).show();
                        NavHostFragment.findNavController(this)
                                .navigate(R.id.action_registerFragment_to_loginFragment);
                        insertDefaultRecipes();
                    } else {
                        Toast.makeText(getContext(), "Registration failed: " + task.getException().getMessage(),
                                Toast.LENGTH_LONG).show();
                    }
                });

    }

    private void insertDefaultRecipes() {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DatabaseReference userRecipesRef = FirebaseDatabase.getInstance()
                .getReference("recipes")
                .child(uid);

        List<Recipe> defaultRecipes = getDefaultRecipes();
        for (Recipe recipe : defaultRecipes) {
            String recipeId = userRecipesRef.push().getKey();
            recipe.setId(recipeId);
            userRecipesRef.child(recipeId).setValue(recipe);
        }

        Toast.makeText(getContext(), "Welcome! Default recipes added.", Toast.LENGTH_SHORT).show();
    }

    private List<Recipe> getDefaultRecipes() {
        List<Recipe> list = new ArrayList<>();

        list.add(new Recipe("Pasta Bolognese",
                "• 500g Pasta\n• 200g Ground beef\n• 1 can Tomato sauce\n• 1 Onion\n• 2 cloves Garlic\n• 2 tbsp Olive oil\n• 1 tbsp Tomato paste\n• Salt, Pepper, Basil",
                "1. Boil the pasta in salted water until al dente.\n2. In a separate pan, heat olive oil, sauté garlic and onion until softened.\n3. Add the ground beef, cook until browned.\n4. Add tomato paste, tomato sauce, and season with salt, pepper, and basil.\n5. Simmer for 10-15 minutes. Serve over pasta.",
                "Medium", "00:30",
                "https://images.unsplash.com/photo-1622973536968-3ead9e780960", false));

        list.add(new Recipe("Simple Salad",
                "• 2 cups Lettuce\n• 1 Tomato\n• 1 Cucumber\n• 1 Carrot\n• Olive oil\n• Lemon juice\n• Salt, Pepper",
                "1. Chop lettuce, tomato, cucumber, and carrot.\n2. Toss everything in a bowl.\n3. Drizzle with olive oil, lemon juice, and season with salt and pepper.",
                "Easy", "00:10",
                "https://images.unsplash.com/photo-1752028935881-0674807b046c?q=80&w=2017", false));

        list.add(new Recipe("Omelette",
                "• 3 Eggs\n• 50g Cheese (Cheddar or Feta)\n• Salt, Pepper\n• 1 tbsp Butter or Oil",
                "1. Crack eggs into a bowl, season with salt and pepper, whisk until smooth.\n2. Heat butter in a pan over medium heat.\n3. Pour in the eggs, cook for 1-2 minutes until edges set, add cheese on top, fold the omelette and serve.",
                "Easy", "00:07",
                "https://images.unsplash.com/photo-1668283653825-37b80f055b05?q=80&w=1171", false));

        list.add(new Recipe("Pancakes",
                "• 1 cup Flour\n• 1 tbsp Sugar\n• 1 tbsp Baking powder\n• 1/4 tsp Salt\n• 1 cup Milk\n• 1 Egg\n• 2 tbsp Butter (melted)\n• 1 tsp Vanilla",
                "1. In a bowl, whisk together flour, sugar, baking powder, and salt.\n2. In a separate bowl, mix milk, egg, melted butter, and vanilla.\n3. Pour wet ingredients into dry ingredients, stir until just combined.\n4. Heat a griddle, pour batter to form pancakes, cook until golden brown.",
                "Easy", "00:20",
                "https://images.unsplash.com/photo-1528207776546-365bb710ee93?q=80&w=1170", false));

        list.add(new Recipe("Chicken Curry",
                "• 1 lb Chicken (boneless, skinless)\n• 1 Onion\n• 2 tbsp Curry paste\n• 1 can Coconut milk\n• 1 tbsp Olive oil\n• 1 cup Chicken broth\n• Salt, Pepper",
                "1. Heat oil in a large pan, sauté onion until translucent.\n2. Add chicken, cook until lightly browned.\n3. Stir in curry paste, cook for 1-2 minutes.\n4. Add coconut milk and chicken broth, simmer for 20 minutes.\n5. Season with salt and pepper, serve with rice.",
                "Hard", "01:00",
                "https://images.unsplash.com/photo-1631292784640-2b24be784d5d", false));

        list.add(new Recipe("Grilled Cheese",
                "• 2 slices Bread\n• 2 slices Cheese (Cheddar or American)\n• Butter",
                "1. Butter one side of each slice of bread.\n2. Place a slice of cheese between the bread slices, butter side out.\n3. Grill in a pan over medium heat until both sides are golden brown and the cheese is melted.",
                "Easy", "00:05",
                "https://images.unsplash.com/photo-1528736235302-52922df5c122?q=80&w=1254", false));

        list.add(new Recipe("Fruit Smoothie",
                "• 1 Banana\n• 1 cup Mixed berries\n• 1/2 cup Yogurt\n• 1/2 cup Milk (or Almond Milk)\n• 1 tbsp Honey",
                "1. Add banana, berries, yogurt, milk, and honey to a blender.\n2. Blend until smooth.\n3. Serve immediately in a glass.",
                "Easy", "00:05",
                "https://images.unsplash.com/photo-1589733955941-5eeaf752f6dd?q=80&w=1171", false));

        list.add(new Recipe("Baked Potatoes",
                "• 4 Potatoes\n• Olive oil\n• Salt, Pepper\n• Fresh herbs (optional)",
                "1. Preheat oven to 400°F (200°C).\n2. Wash and dry the potatoes, prick with a fork.\n3. Rub with olive oil, sprinkle with salt and pepper.\n4. Bake for 40-45 minutes or until tender.\n5. Top with fresh herbs if desired.",
                "Medium", "00:45",
                "https://images.unsplash.com/photo-1633959639799-6d3f66e05710?q=80&w=1074", false));

        return list;
    }

}
