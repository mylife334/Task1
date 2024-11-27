package com.example.task1;// SettingFragment.java

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.Fragment;

import com.example.task1.Authentication.LoginActivity;
import com.example.task1.Database.DatabaseHelper;
import com.example.task1.Model.User;
import com.example.task1.R;

public class SettingFragment extends Fragment {

    private TextView tvName, tvEmail, tvPassword, tvPhone;
    private Button btnEdit, btnLogout;
    private DatabaseHelper databaseHelper;
    private User sessionUser;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        tvName = view.findViewById(R.id.tv_name);
        tvEmail = view.findViewById(R.id.tv_email);
        tvPassword = view.findViewById(R.id.tv_password);
        tvPhone = view.findViewById(R.id.tv_phone);
        btnEdit = view.findViewById(R.id.btn_edit);
        btnLogout = view.findViewById(R.id.btn_logout);

        databaseHelper = new DatabaseHelper(getActivity());
        loadUserInfo();

        btnEdit.setOnClickListener(v -> showEditUserDialog());
        btnLogout.setOnClickListener(v -> showLogoutConfirmationDialog());

        return view;
    }

    private void loadUserInfo() {
        sessionUser = databaseHelper.getSessionUser();
        Log.d("CheckData", " ID for update: " + sessionUser.getId());
        Log.d("CheckData", " name for update: " + sessionUser.getName());
        Log.d("CheckData", " Phone for update: " + sessionUser.getPhone());
        Log.d("CheckData", " PW for update: " + sessionUser.getPassword());
        Log.d("CheckData", " Email for update: " + sessionUser.getEmail());
        if (sessionUser != null) {
            tvName.setText("Name: " + sessionUser.getName());
            tvEmail.setText("Email: " + sessionUser.getEmail());
            tvPassword.setText("Password: ***********"); // Hide password
            tvPhone.setText("Phone: " + sessionUser.getPhone());
        } else {
            Toast.makeText(getActivity(), "Failed to load user info", Toast.LENGTH_SHORT).show();
        }
    }

    private void showEditUserDialog() {
        sessionUser = databaseHelper.getSessionUser();
        int userId = sessionUser.getId();
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_edit_user, null);

        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(getActivity());
        dialogBuilder.setView(dialogView);
        final AlertDialog dialog = dialogBuilder.create();
        dialog.show();

        EditText etName = dialogView.findViewById(R.id.et_name);
        EditText etPhone = dialogView.findViewById(R.id.et_phone);
        EditText etCurrentPassword = dialogView.findViewById(R.id.et_current_password);
        EditText etNewPassword = dialogView.findViewById(R.id.et_new_password);
        EditText etConfirmNewPassword = dialogView.findViewById(R.id.et_confirm_password);
        Button btnSubmit = dialogView.findViewById(R.id.btn_submit);

        // Set initial values for the dialog fields
        etName.setText(sessionUser.getName());
        etPhone.setText(sessionUser.getPhone());

        btnSubmit.setOnClickListener(v -> {
            String newName = etName.getText().toString();
            String newPhone = etPhone.getText().toString();
            String currentPassword = etCurrentPassword.getText().toString();
            String newPassword = etNewPassword.getText().toString();
            String confirmNewPassword = etConfirmNewPassword.getText().toString();

            // Ensure Name and Phone are not empty
            if (TextUtils.isEmpty(newName) || TextUtils.isEmpty(newPhone)) {
                Toast.makeText(getActivity(), "Name and Phone cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if password fields are filled
            if (!TextUtils.isEmpty(currentPassword) || !TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) {
                // Validate the password change if all password fields are filled
                if (TextUtils.isEmpty(currentPassword) || TextUtils.isEmpty(newPassword) || TextUtils.isEmpty(confirmNewPassword)) {
                    Toast.makeText(getActivity(), "All password fields must be filled", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if current password matches the session password
                if (!currentPassword.equals(sessionUser.getPassword())) {
                    Toast.makeText(getActivity(), "Current password is incorrect", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate new password length
                if (newPassword.length() < 6) {
                    Toast.makeText(getActivity(), "New password must be at least 6 characters long", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Check if new passwords match
                if (!newPassword.equals(confirmNewPassword)) {
                    Toast.makeText(getActivity(), "New passwords do not match", Toast.LENGTH_SHORT).show();
                    return;
                }

                // If passwords are valid, update all fields
                User updatedUser = new User(userId, newName, sessionUser.getEmail(), newPassword, newPhone);
                Log.d("Test", "Info: " + updatedUser.getId() + updatedUser.getName() + updatedUser.getEmail() + updatedUser.getPassword() + updatedUser.getPhone());
                databaseHelper.updateUser(updatedUser);
                Toast.makeText(getActivity(), "User updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                // Only update Name and Phone if no password fields are filled
                User updatedUser = new User(userId, newName, sessionUser.getEmail(), sessionUser.getPassword(), newPhone);
                databaseHelper.updateUser(updatedUser);
                Toast.makeText(getActivity(), "User updated successfully", Toast.LENGTH_SHORT).show();
            }

            databaseHelper.deleteSession();
            Intent intent = new Intent(getActivity(),LoginActivity.class);
            startActivity(intent);
            dialog.dismiss();
        });
    }




    private void showLogoutConfirmationDialog() {
        new AlertDialog.Builder(getActivity())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Log Out", (dialog, which) -> {
                    databaseHelper.deleteSession(); // Remove session
                    Toast.makeText(getActivity(), "Logged out successfully", Toast.LENGTH_SHORT).show();
                    // Navigate to login screen
                    // Navigation code here
                    Intent intent = new Intent(getActivity(), LoginActivity.class);
                    startActivity(intent);
                    getActivity().finish();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }
}
