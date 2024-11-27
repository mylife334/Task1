package com.example.task1;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.example.task1.Authentication.Budget.BudgetFragment;
import com.example.task1.Database.DatabaseHelper;
import com.example.task1.ExpenseManager.HomeFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class ExpenseManagerActivity extends AppCompatActivity {

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_expense_manager);

        BottomNavigationView bottomNav = findViewById(R.id.bottom_navigation);
        bottomNav.setOnNavigationItemSelectedListener(navListener);

        databaseHelper = new DatabaseHelper(this);

        // Set default fragment
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        }
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override
                public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    Fragment selectedFragment = null;

                    if (item.getItemId() == R.id.nav_home) {
                        selectedFragment = new HomeFragment();
                    } else if (item.getItemId() == R.id.nav_budget) {
                        selectedFragment = new BudgetFragment();
                    } else if (item.getItemId() == R.id.nav_statistical) {
                        selectedFragment = new StatisticalFragment();
                    } else if (item.getItemId() == R.id.nav_settings) {
                        selectedFragment = new SettingFragment();
                    }


                    getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, selectedFragment).commit();
                    return true;
                }
            };
}
