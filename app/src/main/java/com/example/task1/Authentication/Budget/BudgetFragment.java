package com.example.task1.Authentication.Budget;

import android.app.AlertDialog;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.task1.Database.DatabaseHelper;
import com.example.task1.Model.Category;
import com.example.task1.Model.User;
import com.example.task1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class BudgetFragment extends Fragment {

    private FloatingActionButton fabAddCategory;
    private ListView listViewCategories;
    private DatabaseHelper databaseHelper;
    private int userId; // Ensure userId is passed from activity or fragment
    private int selectedCategoryId;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_budget, container, false);

        fabAddCategory = view.findViewById(R.id.fabAddCategory);
        listViewCategories = view.findViewById(R.id.expensesList);
        LinearLayout budgetSetupLayout = view.findViewById(R.id.BudgetSetup);
        databaseHelper = new DatabaseHelper(getActivity());

        // Gọi phương thức để thiết lập userId từ session
        setUserIdFromSession();

        // Tải dữ liệu ngay sau khi khởi tạo giao diện
        loadCategories();
        fabAddCategory.setOnClickListener(v -> showAddCategoryDialog());

        listViewCategories.setOnItemClickListener((parent, itemView, position, id) -> {
            Category selectedCategory = (Category) parent.getItemAtPosition(position);
            selectedCategoryId = selectedCategory.getId();
            showUpdateCategoryDialog(selectedCategory);
    });

        listViewCategories.setOnItemLongClickListener((parent, view1, position, id) -> {
            Category selectedCategory = (Category) parent.getItemAtPosition(position);
            selectedCategoryId = selectedCategory.getId();
            showDeleteCategoryDialog(selectedCategory);
            return true;
        });

        budgetSetupLayout.setOnClickListener(v -> showSetInitialBudgetDialog());

        return view;
    }
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        loadInitialBudget();
        loadCurrentBudget();
    }

    private void showAddCategoryDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_category, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);
        EditText categoryBudgetEditText = dialogView.findViewById(R.id.categoryBudgetEditText);
        Button btnSaveCategory = dialogView.findViewById(R.id.btnSaveCategory);

        btnSaveCategory.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString();
            String categoryBudget = categoryBudgetEditText.getText().toString();

            if (!categoryName.isEmpty() && !categoryBudget.isEmpty()) {
                double budget = Double.parseDouble(categoryBudget);
                databaseHelper.addCategory(categoryName, budget, userId);
                loadCategories();
                loadInitialBudget();
                loadCurrentBudget();
                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showUpdateCategoryDialog(final Category category) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_category, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText categoryNameEditText = dialogView.findViewById(R.id.categoryNameEditText);
        EditText categoryBudgetEditText = dialogView.findViewById(R.id.categoryBudgetEditText);
        Button btnSaveCategory = dialogView.findViewById(R.id.btnSaveCategory);

        // Set initial values for the dialog fields
        categoryNameEditText.setText(category.getName());
        categoryBudgetEditText.setText(String.valueOf(category.getAmount()));

        btnSaveCategory.setOnClickListener(v -> {
            String categoryName = categoryNameEditText.getText().toString();
            String categoryBudget = categoryBudgetEditText.getText().toString();

            if (!categoryName.isEmpty() && !categoryBudget.isEmpty()) {
                double newBudget = Double.parseDouble(categoryBudget);

                // Get total expenses for the category
                double totalExpenses = databaseHelper.getTotalExpensesForCategory(selectedCategoryId);
                Log.d("UpdateCategory", "New Budget: " + newBudget);
                Log.d("UpdateCategory", "Total Expenses: " + totalExpenses);

                if (newBudget >= totalExpenses) {
                    // Create a new Category object with the updated values
                    Category updatedCategory = new Category(selectedCategoryId, categoryName, newBudget);

                    // Call the update method with the Category object
                    databaseHelper.updateCategory(updatedCategory);

                    loadCategories();
                    loadInitialBudget();
                    loadCurrentBudget();
                    dialog.dismiss();
                } else {
                    // Show warning if new budget is less than total expenses
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Warning")
                            .setMessage("The new budget must be greater than or equal to the current total expenses for this category.")
                            .setPositiveButton("OK", null)
                            .show();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteCategoryDialog(final Category category) {
        // Check if there are any expenses related
        boolean hasExpenses = databaseHelper.hasExpensesForCategory(selectedCategoryId);
        Log.d("DeleteCategory", "Has Expenses: " + hasExpenses + ", Category ID: " + selectedCategoryId);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setTitle("Delete Category");

        if (hasExpenses) {
            // Warning if there are related expenses
            dialogBuilder.setMessage("If you delete this category, all expenses set under this category will also be deleted. Are you sure you want to delete it?");
            dialogBuilder.setPositiveButton("Delete", (dialog, which) -> {
                // Delete all related expenses
                databaseHelper.deleteExpensesForCategory(category.getId());
                // Delete Category
                databaseHelper.deleteCategory(category.getId());
                loadCategories(); // Refresh the list to reflect the changes
                loadInitialBudget();
                loadCurrentBudget();
            });
        } else {
            // Confirmation if there are no related expenses
            dialogBuilder.setMessage("Are you sure you want to delete this category: " + category.getName() + "?");
            dialogBuilder.setPositiveButton("Delete", (dialog, which) -> {
                // Delete Category
                databaseHelper.deleteCategory(category.getId());
                loadCategories(); // Refresh the list to reflect the changes
                loadInitialBudget();
                loadCurrentBudget();
            });
        }

        dialogBuilder.setNegativeButton("Cancel", null);
        dialogBuilder.show();
    }

    private void loadCategories() {
        List<Category> categories = databaseHelper.getCategories(userId);
        CategoryAdapter adapter = new CategoryAdapter(getActivity(), categories);
        listViewCategories.setAdapter(adapter);
    }

    private void setUserIdFromSession() {
        User sessionUser = databaseHelper.getSessionUser();
        if (sessionUser != null) {
            this.userId = sessionUser.getId();
        }
    }

    private void showSetInitialBudgetDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_set_initial_budget, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText initialBudgetEditText = dialogView.findViewById(R.id.initialBudgetEditText);
        Button btnSaveBudget = dialogView.findViewById(R.id.btnSaveBudget);

        // Set initial value in the EditText
        double currentInitialBudget = databaseHelper.getInitialBudget(userId);
        initialBudgetEditText.setText(String.valueOf(currentInitialBudget));

        btnSaveBudget.setOnClickListener(v -> {
            String initialBudgetStr = initialBudgetEditText.getText().toString();
            if (!initialBudgetStr.isEmpty()) {
                double initialBudget = Double.parseDouble(initialBudgetStr);
                databaseHelper.updateInitialBudget(userId, initialBudget);
                loadInitialBudget();
                loadCurrentBudget();
                dialog.dismiss();
            } else {
                Toast.makeText(getActivity(), "Please enter a budget", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadInitialBudget() {
        double initialBudget = databaseHelper.getInitialBudget(userId);
        View view = getView();
        if (view != null) {
            TextView initialBudgetValueTextView = view.findViewById(R.id.initialBudgetValue);
            initialBudgetValueTextView.setText(String.format("$%.2f", initialBudget));
        }
    }
    private double getTotalAmountForCategories() {
        List<Category> categories = databaseHelper.getCategories(userId);
        double totalAmount = 0;
        for (Category category : categories) {
            totalAmount += category.getAmount();  // Giả sử `Category` có phương thức `getAmount()`
        }
        return totalAmount;
    }
    private void loadCurrentBudget() {
        double initialBudget = databaseHelper.getInitialBudget(userId);
        double totalCategoryAmount = getTotalAmountForCategories();
        double currentBudget = initialBudget - totalCategoryAmount;

        // Gán giá trị cho `currentBudgetValue`
        View view = getView();
        if (view != null) {
            TextView currentBudgetLabel = view.findViewById(R.id.currentBudgetLabel);
            TextView currentBudgetValue = view.findViewById(R.id.currentBudgetValue);
            LinearLayout budgetLayout = view.findViewById(R.id.budgetLayout);  // LinearLayout cần đổi màu

            // Gán giá trị cho `currentBudgetValue`
            currentBudgetValue.setText(String.format("$%.2f", currentBudget));

            updateCurrentBudgetColor(currentBudget, initialBudget);
        }
    }
    private void updateCurrentBudgetColor(double currentBudget, double initialBudget) {
        // Tính toán phần trăm ngân sách hiện tại
        double percentage = (currentBudget / initialBudget) * 100;

        // Lấy LinearLayout để thay đổi màu nền
        LinearLayout currentBudgetLayout = getView().findViewById(R.id.currentBudgetLayout);
        if (percentage == 100){
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_green));
        } else if (percentage > 50) {
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_yellow));
        } else if (percentage > 0 && percentage <= 50) {
            // Nếu phần trăm dưới 50% -> màu vàng
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_orange));
        } else {
            // Nếu phần trăm nhỏ hơn 0 -> màu đỏ
            currentBudgetLayout.setBackgroundColor(getResources().getColor(R.color.my_red));
        }
    }



}
