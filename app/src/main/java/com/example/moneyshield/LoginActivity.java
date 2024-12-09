package com.example.moneyshield;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.moneyshield.DbContext;

public class LoginActivity extends AppCompatActivity {
    private DbContext dbHelper;
    private EditText usernameEditText, passwordEditText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DbContext(this);
        usernameEditText = findViewById(R.id.usernameEditText);
        passwordEditText = findViewById(R.id.passwordEditText);
        Button loginButton = findViewById(R.id.loginButton);
        Button btnRegister = findViewById(R.id.btnRegister);

        // Khi người dùng click vào nút đăng nhập
        loginButton.setOnClickListener(v -> {
            String username = usernameEditText.getText().toString().trim();
            String password = passwordEditText.getText().toString().trim();

            if (username.isEmpty() || password.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                return;
            }

            int userId = authenticateUser(username, password);

            if (userId != -1) {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                intent.putExtra("userId", userId);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Hàm xác thực người dùng
    private int authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        int userId = -1;

        try {
            cursor = db.query("users", new String[]{"id"},
                    "username = ? AND password = ?", new String[]{username, password},
                    null, null, null);

            if (cursor != null && cursor.moveToFirst()) {
                userId = cursor.getInt(0);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }

        return userId;
    }

}
