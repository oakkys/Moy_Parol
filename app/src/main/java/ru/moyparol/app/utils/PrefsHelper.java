package ru.moyparol.app.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class PrefsHelper {

    private static final String PREFS_NAME = "moyparol_prefs";
    private static final String KEY_PIN = "pin_hash";
    private static final String KEY_PIN_SET = "pin_set";
    private static final String KEY_BIOMETRIC = "biometric_enabled";

    private final SharedPreferences prefs;

    public PrefsHelper(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public boolean isPinSet() {
        return prefs.getBoolean(KEY_PIN_SET, false);
    }

    public void setPin(String pin) {
        prefs.edit()
            .putString(KEY_PIN, hashPin(pin))
            .putBoolean(KEY_PIN_SET, true)
            .apply();
    }

    public boolean checkPin(String pin) {
        String stored = prefs.getString(KEY_PIN, "");
        return stored.equals(hashPin(pin));
    }

    public boolean isBiometricEnabled() {
        return prefs.getBoolean(KEY_BIOMETRIC, true);
    }

    public void setBiometricEnabled(boolean enabled) {
        prefs.edit().putBoolean(KEY_BIOMETRIC, enabled).apply();
    }

    private String hashPin(String pin) {
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(pin.getBytes("UTF-8"));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) sb.append(String.format("%02x", b));
            return sb.toString();
        } catch (Exception e) {
            return pin;
        }
    }
}
