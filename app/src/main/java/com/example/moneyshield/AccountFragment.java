package com.example.moneyshield;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class AccountFragment extends Fragment {

    private DbContext dbHelper;
    private TextView tvUserName;
    private int userId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account, container, false);

        tvUserName = view.findViewById(R.id.tvUserName);
        Button logoutButton = view.findViewById(R.id.logoutButton);

        dbHelper = new DbContext(requireContext());

        // Kiểm tra và lấy userId từ Bundle
        userId = getArguments() != null ? getArguments().getInt("userId", -1) : -1;

        // Nếu userId hợp lệ, lấy tên người dùng
        if (userId != -1) {
            String username = dbHelper.getUsername(userId);
            if (username != null) {
                tvUserName.setText(username);
            } else {
                tvUserName.setText("Guest");
            }
        } else {
            tvUserName.setText("Guest");
        }

        // Handle logout button click
        logoutButton.setOnClickListener(v -> {
            // Clear session and navigate back to LoginActivity
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            requireActivity().finish();
        });

        return view;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // Đảm bảo đóng DbContext khi Fragment bị hủy
        if (dbHelper != null) {
            dbHelper.close();
        }
    }
}
