package com.example.task1.Authentication;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.task1.Database.DatabaseHelper;
import com.example.task1.ExpenseManagerActivity;
import com.example.task1.Model.User;
import com.example.task1.R;

public class LoginActivity extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private TextView ToRegister;
    private Button btnLogin;
    private DatabaseHelper databaseHelper;
    private CheckBox cbRememberMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etEmail = findViewById(R.id.etEmailLogin);
        etPassword = findViewById(R.id.etPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);
        ToRegister = findViewById(R.id.tvregister);
        cbRememberMe = findViewById(R.id.cbRememberMe);
        databaseHelper = new DatabaseHelper(this);

        // Kiểm tra nếu đã có session, chuyển tới ExpenseManagerActivity
        if (databaseHelper.isSessionActive()) {
            User user = databaseHelper.getSessionUser();
            navigateToExpenseManager(user.getId());
        }

        btnLogin.setOnClickListener(v -> loginUser());
        ToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String email = etEmail.getText().toString();
        String password = etPassword.getText().toString();

        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        SQLiteDatabase db = databaseHelper.getReadableDatabase();
        Cursor cursor = db.query(
                DatabaseHelper.TABLE_USERS,
                new String[]{DatabaseHelper.COLUMN_USER_ID, DatabaseHelper.COLUMN_USER_NAME, DatabaseHelper.COLUMN_USER_EMAIL, DatabaseHelper.COLUMN_USER_PHONE},
                DatabaseHelper.COLUMN_USER_EMAIL + "=? AND " + DatabaseHelper.COLUMN_USER_PASSWORD + "=?",
                new String[]{email, password},
                null,
                null,
                null
        );
        if (cursor != null && cursor.moveToFirst()) {
            int userId = cursor.getInt(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_ID));
            String name = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_NAME));
            String phone = cursor.getString(cursor.getColumnIndex(DatabaseHelper.COLUMN_USER_PHONE));
            cursor.close();

            // Kiểm tra xem checkbox 'Remember Me' có được tích không
            if (cbRememberMe.isChecked()) {
                // Lưu thông tin người dùng vào session
                databaseHelper.saveSession(userId, name, email, phone, password);
            }

            // Chuyển đến ExpenseManagerActivity
            navigateToExpenseManager(userId);
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show();
        }

        if (cursor != null) {
            cursor.close();  // Đảm bảo đóng cursor nếu nó không null
        }
    }


    private void navigateToExpenseManager(int userId) {
        Intent intent = new Intent(this, ExpenseManagerActivity.class);
        intent.putExtra("USER_ID", userId);
        startActivity(intent);
        finish();
    }
}
