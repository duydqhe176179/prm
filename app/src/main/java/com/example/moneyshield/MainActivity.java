package com.example.moneyshield;

import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private int userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        userId = getIntent().getIntExtra("userId", -1);
        if (userId == -1) {
            Toast.makeText(this, "User not found. Please log in again.", Toast.LENGTH_SHORT).show();
            finish(); // Kết thúc nếu không có userId hợp lệ
        }

        if (savedInstanceState == null) {
            loadFragment(createFragmentWithUserId(new HomeFragment()));
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = createFragmentWithUserId(new HomeFragment());
            } else if (item.getItemId() == R.id.transaction) {
                selectedFragment = createFragmentWithUserId(new TransactionBookFragment());
            } else if (item.getItemId() == R.id.add) {
                selectedFragment = createFragmentWithUserId(new AddTransactionFragment());
            } else if (item.getItemId() == R.id.balance) {
                selectedFragment = createFragmentWithUserId(new ReportFragment());
            } else if (item.getItemId() == R.id.account) {
                selectedFragment = createFragmentWithUserId(new AccountFragment());
            }

            if (selectedFragment != null) {
                loadFragment(selectedFragment);
            }
            return true;
        });
    }

    // Helper method to load a fragment into the container
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment);
        transaction.commit();
    }

    // Helper method to pass userId to fragment via Bundle
    private Fragment createFragmentWithUserId(Fragment fragment) {
        Bundle bundle = new Bundle();
        bundle.putInt("userId", userId);
        fragment.setArguments(bundle);
        return fragment;
    }
}
