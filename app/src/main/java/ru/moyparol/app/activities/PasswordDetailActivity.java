package ru.moyparol.app.activities;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.PasswordTransformationMethod;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import ru.moyparol.app.R;
import ru.moyparol.app.database.DatabaseHelper;
import ru.moyparol.app.models.Password;

public class PasswordDetailActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private Password password;
    private boolean passwordVisible = false;
    private TextView tvPassword;
    private TextView btnShowPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_detail);
        db = DatabaseHelper.getInstance(this);

        long id = getIntent().getLongExtra("id", -1);
        if (id == -1) { finish(); return; }

        password = db.getById(id);
        if (password == null) { finish(); return; }

        bindViews();
    }

    private void bindViews() {
        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // Header
        ((TextView) findViewById(R.id.tv_icon)).setText(password.getIconLetter());
        ((TextView) findViewById(R.id.tv_site_name)).setText(password.getSiteName());
        String url = password.getWebsite() != null ? password.getWebsite() : "";
        ((TextView) findViewById(R.id.tv_site_url)).setText(url);

        // Login
        ((TextView) findViewById(R.id.tv_login)).setText(
            password.getLogin() != null ? password.getLogin() : "");
        findViewById(R.id.btn_copy_login).setOnClickListener(v ->
            copyToClipboard("Логин", password.getLogin()));

        // Password
        tvPassword = findViewById(R.id.tv_password);
        tvPassword.setText("••••••••••");
        tvPassword.setTransformationMethod(null);

        btnShowPassword = findViewById(R.id.btn_show_password);
        btnShowPassword.setOnClickListener(v -> togglePassword());

        findViewById(R.id.btn_copy_password).setOnClickListener(v ->
            copyToClipboard("Пароль", password.getPassword()));

        // Website
        ((TextView) findViewById(R.id.tv_website)).setText(
            password.getWebsite() != null ? password.getWebsite() : "");

        // Notes
        if (password.getNotes() != null && !password.getNotes().isEmpty()) {
            ((TextView) findViewById(R.id.tv_notes)).setText(password.getNotes());
            findViewById(R.id.card_notes).setVisibility(android.view.View.VISIBLE);
        }

        // Edit
        findViewById(R.id.btn_edit).setOnClickListener(v -> openEdit());
        findViewById(R.id.btn_edit_header).setOnClickListener(v -> openEdit());

        // Delete
        findViewById(R.id.btn_delete).setOnClickListener(v -> confirmDelete());
    }

    private void togglePassword() {
        passwordVisible = !passwordVisible;
        if (passwordVisible) {
            tvPassword.setText(password.getPassword());
            btnShowPassword.setText("🙈  Скрыть");
        } else {
            tvPassword.setText("••••••••••");
            btnShowPassword.setText("👁  Показать");
        }
    }

    private void copyToClipboard(String label, String text) {
        if (text == null || text.isEmpty()) return;
        ClipboardManager cm = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        cm.setPrimaryClip(ClipData.newPlainText(label, text));
        Toast.makeText(this, label + " скопирован", Toast.LENGTH_SHORT).show();
    }

    private void openEdit() {
        Intent i = new Intent(this, AddPasswordActivity.class);
        i.putExtra("edit_id", password.getId());
        startActivity(i);
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this, R.style.AlertDialogDark)
            .setTitle("Удалить пароль?")
            .setMessage("Пароль будет перемещён в корзину")
            .setPositiveButton("Удалить", (d, w) -> {
                db.softDelete(password.getId());
                Toast.makeText(this, "Перемещён в корзину", Toast.LENGTH_SHORT).show();
                finish();
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Обновляем данные после возврата из редактирования
        if (password != null) {
            password = db.getById(password.getId());
            if (password == null) { finish(); return; }
            // Обновляем только текстовые поля без переназначения слушателей
            ((TextView) findViewById(R.id.tv_icon)).setText(password.getIconLetter());
            ((TextView) findViewById(R.id.tv_site_name)).setText(password.getSiteName());
            String url = password.getWebsite() != null ? password.getWebsite() : "";
            ((TextView) findViewById(R.id.tv_site_url)).setText(url);
            ((TextView) findViewById(R.id.tv_login)).setText(
                password.getLogin() != null ? password.getLogin() : "");
            ((TextView) findViewById(R.id.tv_website)).setText(
                password.getWebsite() != null ? password.getWebsite() : "");
            // Сбрасываем видимость пароля
            passwordVisible = false;
            tvPassword.setText("••••••••••");
            btnShowPassword.setText("👁  Показать");
            // Заметки
            if (password.getNotes() != null && !password.getNotes().isEmpty()) {
                ((TextView) findViewById(R.id.tv_notes)).setText(password.getNotes());
                findViewById(R.id.card_notes).setVisibility(android.view.View.VISIBLE);
            } else {
                findViewById(R.id.card_notes).setVisibility(android.view.View.GONE);
            }
        }
    }
}
