package ru.moyparol.app.activities;

import android.os.Bundle;
import android.text.InputType;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import ru.moyparol.app.R;
import ru.moyparol.app.database.DatabaseHelper;
import ru.moyparol.app.models.Password;
import ru.moyparol.app.utils.CryptoUtil;

public class AddPasswordActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private long editId = -1;
    private Password editPassword;
    private boolean passwordVisible = false;

    private EditText etSite, etLogin, etPassword, etNotes;
    private TextView tvCategory;

    private static final String[] CATEGORIES = {
        "Социальные сети", "Финансы", "Почта", "Работа", "Развлечения", "Wi-Fi", "Другое"
    };
    private String selectedCategory = "Другое";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_password);
        db = DatabaseHelper.getInstance(this);

        editId = getIntent().getLongExtra("edit_id", -1);
        if (editId != -1) {
            editPassword = db.getById(editId);
        }

        bindViews();

        if (editPassword != null) {
            populateForEdit();
        }
    }

    private void bindViews() {
        etSite = findViewById(R.id.et_site);
        etLogin = findViewById(R.id.et_login);
        etPassword = findViewById(R.id.et_password);
        etNotes = findViewById(R.id.et_notes);
        tvCategory = findViewById(R.id.tv_category);

        TextView tvTitle = findViewById(R.id.tv_title);
        tvTitle.setText(editId != -1 ? "Изменить пароль" : "Новый пароль");

        // Cancel
        findViewById(R.id.btn_cancel).setOnClickListener(v -> finish());
        // Save header button
        findViewById(R.id.btn_save_header).setOnClickListener(v -> save());
        // Save button
        findViewById(R.id.btn_save).setOnClickListener(v -> save());

        // Toggle password visibility
        findViewById(R.id.btn_toggle_password).setOnClickListener(v -> {
            passwordVisible = !passwordVisible;
            if (passwordVisible) {
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ((TextView) v).setText("🙈");
            } else {
                etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                ((TextView) v).setText("👁");
            }
            etPassword.setSelection(etPassword.getText().length());
        });

        // Generate password
        findViewById(R.id.btn_generate).setOnClickListener(v -> {
            String generated = CryptoUtil.generatePassword(16, true, true, true);
            etPassword.setText(generated);
            if (!passwordVisible) {
                passwordVisible = true;
                etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                ((TextView) findViewById(R.id.btn_toggle_password)).setText("🙈");
            }
            etPassword.setSelection(generated.length());
            Toast.makeText(this, "Пароль сгенерирован", Toast.LENGTH_SHORT).show();
        });

        // Category picker
        findViewById(R.id.btn_category).setOnClickListener(v -> showCategoryPicker());
    }

    private void populateForEdit() {
        etSite.setText(editPassword.getSiteName());
        etLogin.setText(editPassword.getLogin());
        etPassword.setText(editPassword.getPassword());
        if (editPassword.getNotes() != null) etNotes.setText(editPassword.getNotes());
        if (editPassword.getCategory() != null) {
            selectedCategory = editPassword.getCategory();
            tvCategory.setText(selectedCategory);
        }
    }

    private void showCategoryPicker() {
        new AlertDialog.Builder(this, R.style.AlertDialogDark)
            .setTitle("Категория")
            .setItems(CATEGORIES, (d, which) -> {
                selectedCategory = CATEGORIES[which];
                tvCategory.setText(selectedCategory);
            })
            .setNegativeButton("Отмена", null)
            .show();
    }

    private void save() {
        String site = etSite.getText().toString().trim();
        String login = etLogin.getText().toString().trim();
        String pass = etPassword.getText().toString();
        String notes = etNotes.getText().toString().trim();

        if (site.isEmpty()) {
            etSite.setError("Введите название сайта или приложения");
            etSite.requestFocus();
            return;
        }
        if (pass.isEmpty()) {
            etPassword.setError("Введите пароль");
            etPassword.requestFocus();
            return;
        }

        if (editId != -1 && editPassword != null) {
            editPassword.setSiteName(site);
            editPassword.setLogin(login);
            editPassword.setPassword(pass);
            editPassword.setNotes(notes);
            editPassword.setCategory(selectedCategory);
            editPassword.setWebsite(site.contains(".") ? site : editPassword.getWebsite());
            db.updatePassword(editPassword);
            Toast.makeText(this, "Пароль обновлён", Toast.LENGTH_SHORT).show();
        } else {
            Password p = new Password();
            p.setSiteName(site);
            p.setLogin(login);
            p.setPassword(pass);
            p.setNotes(notes);
            p.setCategory(selectedCategory);
            // auto-detect website from site name
            if (site.contains(".")) {
                p.setWebsite(site);
            } else {
                p.setWebsite(site.toLowerCase().replaceAll("\\s+", "") + ".com");
            }
            db.insertPassword(p);
            Toast.makeText(this, "Пароль сохранён", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
