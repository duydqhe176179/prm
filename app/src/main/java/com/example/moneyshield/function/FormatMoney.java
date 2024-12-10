package com.example.moneyshield.function;

import java.text.DecimalFormat;

public class FormatMoney {
    public static String formatMoney(double amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount);
    }
}
