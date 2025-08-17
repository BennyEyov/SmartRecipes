package com.example.smartrecipes;

import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private OnBackPressedCallback backCallback;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        // אחראי על קצוות האפליקציה שלא יגלשו
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // חיבור ה-navhost שמכיל את כל הפרגמנטים
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            NavController navController = navHostFragment.getNavController();
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
            NavigationUI.setupWithNavController(bottomNavigationView, navController);

            // הצגת/הסתרת ה-BottomNav לפי פרמגנט
            navController.addOnDestinationChangedListener((controller, destination, arguments) -> {
                bottomNavigationView.setVisibility(shouldHideBottomNav(destination) ? View.GONE : View.VISIBLE);

                // דואגים לשליטה על כפתור החזרה בכלל האפליקציה
                // דואגים שה-callback שלנו תמיד יהיה האחרון שנרשם (עדיפות גבוהה)
                if (backCallback != null) backCallback.remove();
                backCallback = new OnBackPressedCallback(true) {
                    @Override
                    public void handleOnBackPressed() {
                        if (navController.getCurrentDestination() == null) {
                            // אם אין יעד – תן לשרשרת להמשיך
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed();
                            setEnabled(true);
                            return;
                        }

                        int destId = navController.getCurrentDestination().getId();

                        if (destId == R.id.homeFragment || destId == R.id.loginFragment) {
                            // כמו קודם: מוציא לרקע
                            moveTaskToBack(true);

                        } else if (destId == R.id.favoritesFragment
                                || destId == R.id.addRecipeFragment
                                || destId == R.id.internetRecipesFragment) {
                            // מסנכרן את ה-BottomNav (הניווט יקרה דרך ה-NavComponent)
                            bottomNavigationView.setSelectedItemId(R.id.homeFragment);

                        } else {
                            // לא אנחנו מטפלים? מעבירים ל-NavHostFragment/מערכת
                            setEnabled(false);
                            getOnBackPressedDispatcher().onBackPressed(); // מדלג ל-callback הבא בשרשרת
                            setEnabled(true);
                        }
                    }
                };
                getOnBackPressedDispatcher().addCallback(this, backCallback);
            });
        }
    }

    // מחליט מתי להסתיר את ה-BottomNavigationView
    private boolean shouldHideBottomNav(NavDestination destination) {
        int id = destination.getId();
        return id == R.id.loginFragment
                || id == R.id.registerFragment
                || id == R.id.recipeFragment
                || id == R.id.editRecipeFragment
                || id == R.id.addRecipeFragment;
    }
}
