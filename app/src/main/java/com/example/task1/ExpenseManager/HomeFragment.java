package com.example.task1.ExpenseManager;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.task1.Database.DatabaseHelper;
import com.example.task1.Model.Expense;
import com.example.task1.Model.User;
import com.example.task1.R;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class HomeFragment extends Fragment {

    private FloatingActionButton fabAddExpense;
    private ListView listViewExpenses;
    private DatabaseHelper databaseHelper;
    private int userId;
    private String selectedDate;
    private TextView totalExpensesValue ;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        fabAddExpense = view.findViewById(R.id.fabAddExpense);
        listViewExpenses = view.findViewById(R.id.expensesList);
        totalExpensesValue = view.findViewById(R.id.totalExpensesValue);
        databaseHelper = new DatabaseHelper(getActivity());
        setUserIdFromSession();
        loadExpenses();
        loadTotal();

        fabAddExpense.setOnClickListener(v -> showAddExpenseDialog());

        return view;
    }

    private void showAddExpenseDialog() {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_add_expense, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText expenseNameEditText = dialogView.findViewById(R.id.expenseName);
        EditText amountEditText = dialogView.findViewById(R.id.expenseAmount);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnAddExpense);

        selectedDate = getCurrentDate();
        btnSelectDate.setText(selectedDate);

        loadCategoriesIntoSpinner(categorySpinner);

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog(btnSelectDate));

        btnSaveExpense.setOnClickListener(v -> {
            String expenseName = expenseNameEditText.getText().toString();
            String amountStr = amountEditText.getText().toString();
            String categoryName = categorySpinner.getSelectedItem().toString();

            if (!expenseName.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    int categoryId = getCategoryIdByName(categoryName);
                    double totalExpenses = databaseHelper.getTotalExpensesByCategoryId(categoryId);
                    double budget = databaseHelper.getCategoryAmount(categoryId);

                    if (totalExpenses + amount > budget) {
                        Toast.makeText(getActivity(), "Exceeds budget limit!", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.addExpense(new Expense(userId, expenseName, amount, categoryId, selectedDate));
                        loadExpenses();
                        loadTotal();
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Invalid amount format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDatePickerDialog(Button dateButton) {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(getActivity(), (view, year1, month1, dayOfMonth) -> {
            Calendar selectedCalendar = Calendar.getInstance();
            selectedCalendar.set(year1, month1, dayOfMonth);
            selectedDate = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(selectedCalendar.getTime());
            dateButton.setText(selectedDate);
        }, year, month, day);

        datePickerDialog.show();
    }

    private void loadCategoriesIntoSpinner(Spinner spinner) {
        List<String> categoryNames = databaseHelper.getAllCategoryNames(userId);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getActivity(),
                android.R.layout.simple_spinner_item,
                categoryNames
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
    }

    private void loadExpenses() {
        List<Expense> expenses = databaseHelper.getExpensesByUser(userId);
        ExpenseAdapter adapter = new ExpenseAdapter(getActivity(), expenses,
                expense -> showUpdateExpenseDialog(expense),
                expense -> showDeleteConfirmationDialog(expense) // Thêm listener xóa
        );
        listViewExpenses.setAdapter(adapter);
    }
    private void loadTotal(){
        double totalExpenses = databaseHelper.getTotalExpenses(userId);
        totalExpensesValue.setText(String.format("$%.2f", totalExpenses));
    }

    private void setUserIdFromSession() {
        User sessionUser = databaseHelper.getSessionUser();
        if (sessionUser != null) {
            this.userId = sessionUser.getId();
        }
    }

    private String getCurrentDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        return sdf.format(new Date());
    }

    private int getCategoryIdByName(String categoryName) {
        return databaseHelper.getCategoryIdByName(userId, categoryName);
    }

    private void showUpdateExpenseDialog(Expense expense) {
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_expense, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText expenseNameEditText = dialogView.findViewById(R.id.expenseName);
        EditText amountEditText = dialogView.findViewById(R.id.expenseAmount);
        Spinner categorySpinner = dialogView.findViewById(R.id.categorySpinner);
        Button btnSelectDate = dialogView.findViewById(R.id.btnSelectDate);
        Button btnSaveExpense = dialogView.findViewById(R.id.btnSaveExpense);

        expenseNameEditText.setText(expense.getName());
        amountEditText.setText(String.valueOf(expense.getAmount()));
        btnSelectDate.setText(expense.getDate());
        selectedDate = expense.getDate();

        loadCategoriesIntoSpinner(categorySpinner);
        categorySpinner.setSelection(((ArrayAdapter<String>) categorySpinner.getAdapter()).getPosition(databaseHelper.getCategoryNameById(expense.getCategoryId())));

        btnSelectDate.setOnClickListener(v -> showDatePickerDialog(btnSelectDate));

        btnSaveExpense.setOnClickListener(v -> {
            String expenseName = expenseNameEditText.getText().toString();
            String amountStr = amountEditText.getText().toString();
            String categoryName = categorySpinner.getSelectedItem().toString();

            if (!expenseName.isEmpty() && !amountStr.isEmpty()) {
                try {
                    double amount = Double.parseDouble(amountStr);
                    int categoryId = getCategoryIdByName(categoryName);
                    double totalExpenses = databaseHelper.getTotalExpensesByCategoryId(categoryId);
                    double previousAmount = expense.getAmount();
                    double budget = databaseHelper.getCategoryAmount(categoryId);

                    if (totalExpenses - previousAmount + amount > budget) {
                        Toast.makeText(getActivity(), "Exceeds budget limit!", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseHelper.updateExpense(new Expense(expense.getId(), userId, expenseName, amount, categoryId, selectedDate));
                        loadExpenses();
                        loadTotal();
                        dialog.dismiss();
                    }
                } catch (NumberFormatException e) {
                    Toast.makeText(getActivity(), "Invalid amount format", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(getActivity(), "Please fill out all fields", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDeleteConfirmationDialog(Expense expense) {
        new AlertDialog.Builder(getActivity())
                .setTitle("Confirm Delete")
                .setMessage("Are you sure you want to delete this expense?")
                .setPositiveButton("Yes", (dialog, which) -> {
                    databaseHelper.deleteExpense(expense.getId());
                    loadExpenses();
                    loadTotal();
                })
                .setNegativeButton("No", null)
                .show();
    }
}
