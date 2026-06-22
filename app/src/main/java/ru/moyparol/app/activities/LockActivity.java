package ru.moyparol.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;

import java.util.concurrent.Executor;

import ru.moyparol.app.R;
import ru.moyparol.app.utils.PrefsHelper;

public class LockActivity extends AppCompatActivity {

    private PrefsHelper prefs;
    private StringBuilder pinInput = new StringBuilder();
    private static final int PIN_LENGTH = 5;

    private View[] dotViews;
    private TextView tvSubtitle;
    private boolean settingPin = false;
    private String firstPin = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);

        prefs = new PrefsHelper(this);

        dotViews = new View[]{
            findViewById(R.id.dot1), findViewById(R.id.dot2),
            findViewById(R.id.dot3), findViewById(R.id.dot4),
            findViewById(R.id.dot5)
        };
        tvSubtitle = findViewById(R.id.tv_subtitle);

        if (!prefs.isPinSet()) {
            settingPin = true;
            tvSubtitle.setText("Создайте PIN-код");
        }

        setupNumpad();

        findViewById(R.id.btn_biometric).setOnClickListener(v -> showBiometricPrompt());

        if (prefs.isPinSet() && prefs.isBiometricEnabled()) {
            new Handler(Looper.getMainLooper()).postDelayed(this::showBiometricPrompt, 300);
        }
    }

    private void setupNumpad() {
        int[] numIds = {
            R.id.btn0, R.id.btn1, R.id.btn2, R.id.btn3, R.id.btn4,
            R.id.btn5, R.id.btn6, R.id.btn7, R.id.btn8, R.id.btn9
        };
        int[] nums = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};

        for (int i = 0; i < numIds.length; i++) {
            final int num = nums[i];
            View btn = findViewById(numIds[i]);
            if (btn != null) {
                btn.setOnClickListener(v -> appendPin(String.valueOf(num)));
            }
        }

        View btnDel = findViewById(R.id.btn_delete);
        if (btnDel != null) btnDel.setOnClickListener(v -> deletePin());
    }

    private void appendPin(String digit) {
        if (pinInput.length() >= PIN_LENGTH) return;
        pinInput.append(digit);
        updateDots();
        if (pinInput.length() == PIN_LENGTH) {
            new Handler(Looper.getMainLooper()).postDelayed(this::processPin, 200);
        }
    }

    private void deletePin() {
        if (pinInput.length() > 0) {
            pinInput.deleteCharAt(pinInput.length() - 1);
            updateDots();
        }
    }

    private void updateDots() {
        for (int i = 0; i < dotViews.length; i++) {
            dotViews[i].setBackgroundResource(i < pinInput.length()
                ? R.drawable.pin_dot_filled : R.drawable.pin_dot_empty);
        }
    }

    private void processPin() {
        String pin = pinInput.toString();
        pinInput.setLength(0);
        updateDots();

        if (settingPin) {
            if (firstPin == null) {
                firstPin = pin;
                tvSubtitle.setText("Повторите PIN-код");
            } else {
                if (firstPin.equals(pin)) {
                    prefs.setPin(pin);
                    openHome();
                } else {
                    firstPin = null;
                    showError("PIN-коды не совпадают");
                    tvSubtitle.setText("Создайте PIN-код");
                }
            }
        } else {
            if (prefs.checkPin(pin)) {
                openHome();
            } else {
                showError("Неверный PIN-код");
            }
        }
    }

    private void showError(String msg) {
        tvSubtitle.setText(msg);
        tvSubtitle.setTextColor(getColor(R.color.error_red));
        for (View dot : dotViews) {
            dot.setBackgroundResource(R.drawable.pin_dot_error);
        }
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            tvSubtitle.setText(settingPin ? "Создайте PIN-код" : "Введите PIN-код для доступа");
            tvSubtitle.setTextColor(getColor(R.color.text_secondary));
            updateDots();
        }, 1000);
    }

    private void openHome() {
        startActivity(new Intent(this, HomeActivity.class));
        finish();
    }

    private void showBiometricPrompt() {
        BiometricManager bm = BiometricManager.from(this);
        if (bm.canAuthenticate(BiometricManager.Authenticators.BIOMETRIC_WEAK) != BiometricManager.BIOMETRIC_SUCCESS) return;

        Executor executor = ContextCompat.getMainExecutor(this);
        BiometricPrompt prompt = new BiometricPrompt(this, executor, new BiometricPrompt.AuthenticationCallback() {
            @Override
            public void onAuthenticationSucceeded(@NonNull BiometricPrompt.AuthenticationResult result) {
                openHome();
            }

            @Override
            public void onAuthenticationError(int errorCode, @NonNull CharSequence errString) {}

            @Override
            public void onAuthenticationFailed() {}
        });

        BiometricPrompt.PromptInfo info = new BiometricPrompt.PromptInfo.Builder()
            .setTitle("Мой пароль")
            .setSubtitle("Войдите с помощью биометрии")
            .setNegativeButtonText("PIN-код")
            .build();

        prompt.authenticate(info);
    }
}
