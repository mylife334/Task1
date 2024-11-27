package com.example.task1.Database;

import android.content.Context;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.example.task1.Model.Category;
import com.example.task1.Model.Expense;
import com.example.task1.Model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "expense_manager.db";
    private static final int DATABASE_VERSION = 5;

    // Table and column names
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_USER_ID = "id";
    public static final String COLUMN_USER_NAME = "name";
    public static final String COLUMN_USER_EMAIL = "email";
    public static final String COLUMN_USER_PASSWORD = "password";
    public static final String COLUMN_USER_PHONE = "phone";
    public static final String COLUMN_USER_INITIAL = "initial_budget";

    public static final String TABLE_CATEGORIES = "categories";
    public static final String COLUMN_CATEGORY_ID = "id";
    public static final String COLUMN_CATEGORY_USER_ID = "user_id";
    public static final String COLUMN_CATEGORY_NAME = "name";
    public static final String COLUMN_CATEGORY_AMOUNT = "amount";

    public static final String TABLE_EXPENSES = "expenses";
    public static final String COLUMN_EXPENSE_ID = "id";
    public static final String COLUMN_EXPENSE_NAME = "name";
    public static final String COLUMN_EXPENSE_USER_ID = "user_id";
    public static final String COLUMN_EXPENSE_AMOUNT = "amount";
    public static final String COLUMN_EXPENSE_CATEGORY_ID = "category_id";
    public static final String COLUMN_EXPENSE_DATE = "date";

    public static final String TABLE_SESSION = "session";
    public static final String COLUMN_SESSION_USER_ID = "user_id";
    public static final String COLUMN_SESSION_NAME = "name";
    public static final String COLUMN_SESSION_EMAIL = "email";
    public static final String COLUMN_SESSION_PHONE = "phone";
    public static final String COLUMN_SESSION_PASSWORD = "password";

    private static final String TABLE_CREATE_USERS =
            "CREATE TABLE " + TABLE_USERS + " (" +
                    COLUMN_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_USER_NAME + " TEXT, " +
                    COLUMN_USER_EMAIL + " TEXT UNIQUE, " +
                    COLUMN_USER_PASSWORD + " TEXT, " +
                    COLUMN_USER_PHONE + " TEXT, " +
                    COLUMN_USER_INITIAL + " REAL DEFAULT 0" + // Thêm trường initial_budget
                    ");";

    private static final String TABLE_CREATE_CATEGORIES =
            "CREATE TABLE " + TABLE_CATEGORIES + " (" +
                    COLUMN_CATEGORY_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_CATEGORY_USER_ID + " INTEGER, " +
                    COLUMN_CATEGORY_NAME + " TEXT, " +
                    COLUMN_CATEGORY_AMOUNT + " REAL, " + // New column
                    "FOREIGN KEY(" + COLUMN_CATEGORY_USER_ID + ") REFERENCES " +
                    TABLE_USERS + "(" + COLUMN_USER_ID + ")" +
                    ");";

    private static final String TABLE_CREATE_EXPENSES =
            "CREATE TABLE " + TABLE_EXPENSES + "("
                    + COLUMN_EXPENSE_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + COLUMN_EXPENSE_NAME + " TEXT,"
                    + COLUMN_EXPENSE_USER_ID + " INTEGER,"
                    + COLUMN_EXPENSE_AMOUNT + " REAL,"
                    + COLUMN_EXPENSE_CATEGORY_ID + " INTEGER,"
                    + COLUMN_EXPENSE_DATE + " TEXT,"
                    + "FOREIGN KEY(" + COLUMN_EXPENSE_CATEGORY_ID + ") REFERENCES "
                    + TABLE_CATEGORIES + "(" + COLUMN_CATEGORY_ID + "))";

    private static final String TABLE_CREATE_SESSION =
            "CREATE TABLE " + TABLE_SESSION + " (" +
                    COLUMN_SESSION_USER_ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_SESSION_NAME + " TEXT, " +
                    COLUMN_SESSION_EMAIL + " TEXT, " +
                    COLUMN_SESSION_PHONE + " TEXT, " +
                    COLUMN_SESSION_PASSWORD + " TEXT" +
                    ");";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(TABLE_CREATE_USERS);
        db.execSQL(TABLE_CREATE_CATEGORIES);
        db.execSQL(TABLE_CREATE_EXPENSES);
        db.execSQL(TABLE_CREATE_SESSION);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CATEGORIES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SESSION);
        onCreate(db);
    }

    // Add these methods to manage session data

    public void saveSession(int userId, String name, String email, String phone, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_SESSION_USER_ID, userId);
        values.put(COLUMN_SESSION_NAME, name);
        values.put(COLUMN_SESSION_EMAIL, email);
        values.put(COLUMN_SESSION_PHONE, phone);
        values.put(COLUMN_SESSION_PASSWORD, password);
        db.replace(TABLE_SESSION, null, values);  // Use replace to update or insert new session
        db.close();
    }
    public boolean isSessionActive() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSION, null, null, null, null, null, null);
        boolean sessionExists = cursor.moveToFirst();
        cursor.close();
        return sessionExists;
    }

    // Method to get session data
    public User getSessionUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SESSION, null, null, null, null, null, null);
        if (cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(COLUMN_SESSION_USER_ID));
            String name = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_NAME));
            String email = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_EMAIL));
            String phone = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_PHONE));
            String password = cursor.getString(cursor.getColumnIndex(COLUMN_SESSION_PASSWORD));
            cursor.close();
            return new User(userId, name, email, password, phone);
        }
        cursor.close();
        return null;
    }


    //Budget Tab
    public void addCategory(String name, double amount, int userId) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, name);
        values.put(COLUMN_CATEGORY_AMOUNT, amount);
        values.put(COLUMN_CATEGORY_USER_ID, userId);
        db.insert(TABLE_CATEGORIES, null, values);
        db.close();
    }
    public void updateCategory(Category category) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_CATEGORY_NAME, category.getName());
        values.put(COLUMN_CATEGORY_AMOUNT, category.getAmount());

        String whereClause = COLUMN_CATEGORY_ID + "=?";
        String[] whereArgs = { String.valueOf(category.getId()) };

        db.update(TABLE_CATEGORIES, values, whereClause, whereArgs);
        db.close();
    }
    public void deleteCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        String whereClause = COLUMN_CATEGORY_ID + "=?";
        String[] whereArgs = { String.valueOf(categoryId) };

        db.delete(TABLE_CATEGORIES, whereClause, whereArgs);
        db.close();
    }




    public List<Category> getCategories(int userId) {
        List<Category> categories = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{COLUMN_CATEGORY_ID, COLUMN_CATEGORY_NAME, COLUMN_CATEGORY_AMOUNT},
                COLUMN_CATEGORY_USER_ID + "=?",
                new String[]{String.valueOf(userId)},
                null, null, null);

        if (cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex(COLUMN_CATEGORY_ID));
                String name = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
                double amount = cursor.getDouble(cursor.getColumnIndex(COLUMN_CATEGORY_AMOUNT));
                categories.add(new Category(id, name, amount));
            } while (cursor.moveToNext());
        }
        cursor.close();
        db.close();
        return categories;
    }
    public double getTotalExpensesForCategory(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT SUM(" + COLUMN_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_EXPENSE_CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        double total = 0;
        if (cursor.moveToFirst()) {
            total = cursor.getDouble(0);
        }
        cursor.close();
        db.close();
        return total;
    }


    public boolean hasExpensesForCategory(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT COUNT(*) FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_EXPENSE_CATEGORY_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(categoryId)});

        boolean hasExpenses = false;
        if (cursor.moveToFirst()) {
            int count = cursor.getInt(0);
            hasExpenses = count > 0;
        }
        cursor.close();
        db.close();
        return hasExpenses;
    }



    // Xóa tất cả Expenses liên quan đến Category
    public void deleteExpensesForCategory(int categoryId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSES, COLUMN_EXPENSE_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)});
        db.close();
    }


    // Add a new expense
    // Get all expenses for a specific user
    public List<Expense> getExpensesByUser(int userId) {
        List<Expense> expenses = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSES, null, "user_id=?", new String[]{String.valueOf(userId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndex("id"));
                String name = cursor.getString(cursor.getColumnIndex("name"));
                double amount = cursor.getDouble(cursor.getColumnIndex("amount"));
                int categoryId = cursor.getInt(cursor.getColumnIndex("category_id"));
                String date = cursor.getString(cursor.getColumnIndex("date"));

                expenses.add(new Expense(id, userId, name, amount, categoryId, date));
            } while (cursor.moveToNext());

            cursor.close();
        }

        return expenses;
    }



    // Add a new expense
    public double getTotalExpensesByCategoryId(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(" + COLUMN_EXPENSE_AMOUNT + ") FROM " + TABLE_EXPENSES + " WHERE " + COLUMN_EXPENSE_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)});

        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getDouble(0);
        }
        return 0.0;
    }

    // Lấy ngân sách của danh mục theo ID
    public double getCategoryAmount(int categoryId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_CATEGORIES, new String[]{COLUMN_CATEGORY_AMOUNT}, COLUMN_CATEGORY_ID + "=?", new String[]{String.valueOf(categoryId)}, null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            return cursor.getDouble(cursor.getColumnIndex(COLUMN_CATEGORY_AMOUNT));
        }
        return 0.0;
    }
    public double getTotalExpenses(int userId) {
        double totalExpenses = 0.0;
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT SUM(" + COLUMN_EXPENSE_AMOUNT + ") as Total FROM " + TABLE_EXPENSES +
                " WHERE user_id = ?";

        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(userId) });

        if (cursor.moveToFirst()) {
            totalExpenses = cursor.getDouble(cursor.getColumnIndex("Total"));
        }

        cursor.close();
        db.close();

        return totalExpenses;
    }


    // Thêm chi tiêu
    public void addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_EXPENSE_NAME, expense.getName());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_USER_ID, expense.getUserId());
        values.put(COLUMN_EXPENSE_CATEGORY_ID, expense.getCategoryId());
        values.put(COLUMN_EXPENSE_DATE, expense.getDate());
        db.insert(TABLE_EXPENSES, null, values);
    }


    public int updateExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        if (expense.getId() <= 0) {
            Log.e("AddExpenseDialog", "Invalid expense ID: " + expense.getId());

        }
        // Đảm bảo các cột này đúng tên như trong schema
        values.put(COLUMN_EXPENSE_NAME, expense.getName());
        values.put(COLUMN_EXPENSE_AMOUNT, expense.getAmount());
        values.put(COLUMN_EXPENSE_USER_ID, expense.getUserId());
        values.put(COLUMN_EXPENSE_CATEGORY_ID, expense.getCategoryId());
        values.put(COLUMN_EXPENSE_DATE, expense.getDate());

        // Cập nhật bản ghi trong database dựa vào ID
        int rowsAffected = db.update(TABLE_EXPENSES, values, COLUMN_EXPENSE_ID + "=?",
                new String[]{String.valueOf(expense.getId())});

        // Đóng kết nối với database sau khi update
        db.close();

        // Kiểm tra kết quả của việc cập nhật, nếu không có dòng nào bị ảnh hưởng
        if (rowsAffected == 0) {
            // Log thông tin hoặc xử lý nếu cần
            Log.e("Database", "Update failed, no row affected for expense id: " + expense.getId());
        }
        return rowsAffected;
    }

    public String getCategoryNameById(int categoryId) {
        SQLiteDatabase db = getReadableDatabase();
        String categoryName = null;
        Cursor cursor = db.query(TABLE_CATEGORIES,
                new String[]{COLUMN_CATEGORY_NAME},
                COLUMN_CATEGORY_ID + "=?",
                new String[]{String.valueOf(categoryId)},
                null, null, null);

        if (cursor != null && cursor.moveToFirst()) {
            categoryName = cursor.getString(cursor.getColumnIndex(COLUMN_CATEGORY_NAME));
            cursor.close();
        }
        db.close();
        return categoryName;
    }


    public List<String> getAllCategoryNames(int userId) {
        List<String> categoryNames = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT name FROM categories WHERE user_id = ?", new String[]{String.valueOf(userId)});

        if (cursor.moveToFirst()) {
            do {
                String categoryName = cursor.getString(0);
                categoryNames.add(categoryName);
                // Log để kiểm tra
                Log.d("DatabaseHelper", "Category found: " + categoryName);
            } while (cursor.moveToNext());
        } else {
            Log.d("DatabaseHelper", "No categories found for userId: " + userId);
        }
        cursor.close();
        db.close();
        return categoryNames;
    }



    public int getCategoryIdByName(int userId, String categoryName) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT id FROM Categories WHERE user_id = ? AND name = ?";
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(userId), categoryName});

        if (cursor.moveToFirst()) {
            int categoryId = cursor.getInt(0);
            cursor.close();
            db.close();
            return categoryId;
        }
        cursor.close();
        db.close();
        return -1; // Nếu không tìm thấy categoryId
    }



    public void deleteExpense(int expenseId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete("expenses", "id = ?", new String[]{String.valueOf(expenseId)});
        db.close();
    }
    public double getInitialBudget(int userId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("users", new String[]{"initial_budget"},
                "id = ?", new String[]{String.valueOf(userId)},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            double initialBudget = cursor.getDouble(cursor.getColumnIndex("initial_budget"));
            cursor.close();
            return initialBudget;
        }
        return 0.0;
    }

    public void updateInitialBudget(int userId, double initialBudget) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("initial_budget", initialBudget);
        db.update("users", values, "id = ?", new String[]{String.valueOf(userId)});
    }

// DatabaseHelper.java

    public boolean updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();

        // Insert new values for each column
        values.put(COLUMN_USER_NAME, user.getName());
        values.put(COLUMN_USER_EMAIL, user.getEmail());
        values.put(COLUMN_USER_PHONE, user.getPhone());
        values.put(COLUMN_USER_PASSWORD, user.getPassword());

        // Update the user row with the given userId
        int result = db.update(TABLE_USERS, values, COLUMN_USER_ID + " = ?", new String[]{String.valueOf(user.getId())});
        return result > 0; // If result > 0, the update was successful

    }

// DatabaseHelper.java

    public void deleteSession() {
        SQLiteDatabase db = this.getWritableDatabase();

        // Delete all records from the session table
        int rowsDeleted = db.delete("session", null, null);

        if (rowsDeleted > 0) {
            Log.d("DatabaseHelper", "Session deleted successfully.");
        } else {
            Log.d("DatabaseHelper", "Session deletion failed.");
        }
    }


}
