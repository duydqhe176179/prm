package com.example.moneyshield;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    private int userId = 123; // Giá trị userId giả lập, thay bằng giá trị thực tế

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load HomeFragment by default
        if (savedInstanceState == null) {
            loadFragment(createFragmentWithUserId(new HomeFragment()));
        }

        // Set up BottomNavigationView to handle tab navigation
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
            Fragment selectedFragment = null;

            if (item.getItemId() == R.id.home) {
                selectedFragment = createFragmentWithUserId(new HomeFragment());
            } else if (item.getItemId() == R.id.transaction || item.getItemId() == R.id.add) {
                selectedFragment = createFragmentWithUserId(new AddTransactionFragment());
            } else if (item.getItemId() == R.id.balance) {
                selectedFragment = createFragmentWithUserId(new ReportFragment());
            } else if (item.getItemId() == R.id.account) {
                selectedFragment = createFragmentWithUserId(new AccountFragment());
            }

            // Replace the current fragment with the selected one
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
