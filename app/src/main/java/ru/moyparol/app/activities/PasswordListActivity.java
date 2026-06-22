package ru.moyparol.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import ru.moyparol.app.R;
import ru.moyparol.app.adapters.PasswordAdapter;
import ru.moyparol.app.database.DatabaseHelper;
import ru.moyparol.app.models.Password;

public class PasswordListActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private PasswordAdapter adapter;
    private String filter;
    private RecyclerView rv;
    private View layoutEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_password_list);
        db = DatabaseHelper.getInstance(this);
        filter = getIntent().getStringExtra("filter");
        if (filter == null) filter = "all";

        // Title
        TextView tvTitle = findViewById(R.id.tv_title);
        switch (filter) {
            case "security": tvTitle.setText("Безопасность"); break;
            case "wifi": tvTitle.setText("Wi-Fi"); break;
            default: tvTitle.setText("Все пароли"); break;
        }

        // Back
        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        // RecyclerView
        rv = findViewById(R.id.rv_passwords);
        layoutEmpty = findViewById(R.id.layout_empty);
        adapter = new PasswordAdapter(p -> {
            Intent i = new Intent(this, PasswordDetailActivity.class);
            i.putExtra("id", p.getId());
            startActivity(i);
        });
        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        // Search
        EditText etSearch = findViewById(R.id.et_search);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int st, int c, int a) {}
            @Override public void afterTextChanged(Editable s) {}
            @Override public void onTextChanged(CharSequence s, int st, int b, int c) {
                loadPasswords(s.toString().trim());
            }
        });

        loadPasswords("");
    }

    @Override
    protected void onResume() {
        super.onResume();
        EditText et = findViewById(R.id.et_search);
        loadPasswords(et.getText().toString().trim());
    }

    private void loadPasswords(String query) {
        List<Password> list;
        if (!query.isEmpty()) {
            list = db.search(query);
        } else {
            switch (filter) {
                case "security":
                    list = db.getAllActive();
                    list.removeIf(p -> !p.isWeak());
                    break;
                case "wifi":
                    list = db.getByCategory("Wi-Fi");
                    break;
                default:
                    list = db.getAllActive();
            }
        }
        if (query.isEmpty() && !filter.equals("wifi")) {
            adapter.setPasswords(list); // with section headers
        } else {
            adapter.setPasswordsFlat(list);
        }
        rv.setVisibility(list.isEmpty() ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
    }
}
