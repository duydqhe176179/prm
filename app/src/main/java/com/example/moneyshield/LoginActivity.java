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
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = usernameEditText.getText().toString().trim();
                String password = passwordEditText.getText().toString().trim();

                // Kiểm tra xem các trường có rỗng không
                if (username.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in both fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Xác thực người dùng
                if (authenticateUser(username, password)) {
                    // Chuyển hướng tới MainActivity nếu đăng nhập thành công
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    intent.putExtra("username", username);  // Truyền tên người dùng vào MainActivity
                    startActivity(intent);  // Chuyển màn hình
                    finish(); // Đóng LoginActivity để không quay lại được
                } else {
                    // Thông báo nếu đăng nhập không thành công
                    Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Khi người dùng click vào nút đăng ký
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });
    }

    // Hàm xác thực người dùng
    private boolean authenticateUser(String username, String password) {
        SQLiteDatabase db = dbHelper.getReadableDatabase();
        Cursor cursor = null;
        boolean isAuthenticated = false;

        try {
            // Truy vấn để tìm người dùng với tên đăng nhập và mật khẩu tương ứng
            cursor = db.query("users", new String[]{"id", "username", "password"},
                    "username = ? AND password = ?", new String[]{username, password},
                    null, null, null);

            // Kiểm tra xem có kết quả không
            if (cursor != null && cursor.moveToFirst()) {
                isAuthenticated = true;
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // Đảm bảo đóng con trỏ sau khi sử dụng
            if (cursor != null) {
                cursor.close();
            }
        }

        return isAuthenticated;
    }
}
