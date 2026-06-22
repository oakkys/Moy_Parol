package ru.moyparol.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import ru.moyparol.app.R;
import ru.moyparol.app.database.DatabaseHelper;

public class HomeActivity extends AppCompatActivity {

    private DatabaseHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        db = DatabaseHelper.getInstance(this);
        setupCards();
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateCounts();
    }

    private void setupCards() {
        findViewById(R.id.card_all).setOnClickListener(v -> {
            Intent i = new Intent(this, PasswordListActivity.class);
            i.putExtra("filter", "all");
            startActivity(i);
        });

        findViewById(R.id.card_security).setOnClickListener(v -> {
            Intent i = new Intent(this, PasswordListActivity.class);
            i.putExtra("filter", "security");
            startActivity(i);
        });

        findViewById(R.id.card_deleted).setOnClickListener(v ->
            startActivity(new Intent(this, DeletedPasswordsActivity.class)));

        findViewById(R.id.card_wifi).setOnClickListener(v -> {
            Intent i = new Intent(this, PasswordListActivity.class);
            i.putExtra("filter", "wifi");
            startActivity(i);
        });

        findViewById(R.id.fab_add).setOnClickListener(v ->
            startActivity(new Intent(this, AddPasswordActivity.class)));
    }

    private void updateCounts() {
        int total = db.countActive();
        int deleted = db.countDeleted();
        int weak = db.countWeak();

        ((TextView) findViewById(R.id.tv_all_count)).setText(plural(total, "паролей", "пароль", "пароля"));
        ((TextView) findViewById(R.id.tv_security_count)).setText(plural(weak, "слабых", "слабый", "слабых"));
        ((TextView) findViewById(R.id.tv_deleted_count)).setText(plural(deleted, "паролей", "пароль", "пароля"));

        int wifiCount = db.getByCategory("Wi-Fi").size();
        ((TextView) findViewById(R.id.tv_wifi_count)).setText(plural(wifiCount, "сетей", "сеть", "сети"));

        // Показываем предупреждение о слабых паролях
        android.view.View warningCard = findViewById(R.id.card_security_warning);
        if (warningCard != null) {
            warningCard.setVisibility(weak > 0 ? android.view.View.VISIBLE : android.view.View.GONE);
            TextView tvWarningCount = findViewById(R.id.tv_security_warning_count);
            if (tvWarningCount != null) {
                tvWarningCount.setText(plural(weak, "слабых паролей", "слабый пароль", "слабых пароля"));
            }
        }
    }

    private String plural(int n, String few, String one, String two) {
        int mod10 = n % 10, mod100 = n % 100;
        String word;
        if (mod100 >= 11 && mod100 <= 14) word = few;
        else if (mod10 == 1) word = one;
        else if (mod10 >= 2 && mod10 <= 4) word = two;
        else word = few;
        return n + " " + word;
    }
}
