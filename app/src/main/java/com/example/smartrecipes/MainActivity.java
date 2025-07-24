package com.example.smartrecipes;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        // חיבור ל-Navigation Component
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // האזן לשינויים ביעד הניווט
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                if (shouldHideBottomNav(destination)) {
                    bottomNavigationView.setVisibility(View.GONE);
                } else {
                    bottomNavigationView.setVisibility(View.VISIBLE);
                }
            });
        }

    }

    // מחליט מתי להסתיר את ה־BottomNavigationView
    private boolean shouldHideBottomNav(NavDestination destination) {
        int id = destination.getId();

        return id == R.id.loginFragment ||
                id == R.id.registerFragment ||
                id == R.id.recipeFragment ||
                id == R.id.editRecipeFragment;
    }


    @Override
    public void onBackPressed() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

        // אם אתה נמצא ב-HomeFragment
        if (navController.getCurrentDestination() != null) {
            if (navController.getCurrentDestination().getId() == R.id.homeFragment ||
                    navController.getCurrentDestination().getId() == R.id.loginFragment) {
                moveTaskToBack(true); // מכניס את האפליקציה לרקע
            } else if (navController.getCurrentDestination().getId() == R.id.favoritesFragment ||
                    navController.getCurrentDestination().getId() == R.id.addRecipeFragment ||
                    navController.getCurrentDestination().getId() == R.id.internetRecipesFragment
            ) {
                // סנכרון ה-BottomNavigationView עם ה-NavController
                bottomNavigationView.setSelectedItemId(R.id.homeFragment);
                // ניווט חזרה למסך הבית
                //navController.navigate(R.id.homeFragment);
            } else {
                super.onBackPressed(); // אם לא, תבצע את ההתנהגות הרגילה
            }
        }
    }



}
