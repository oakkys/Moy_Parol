package ru.moyparol.app.activities;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import ru.moyparol.app.R;
import ru.moyparol.app.adapters.DeletedPasswordAdapter;
import ru.moyparol.app.database.DatabaseHelper;
import ru.moyparol.app.models.Password;

public class DeletedPasswordsActivity extends AppCompatActivity {

    private DatabaseHelper db;
    private DeletedPasswordAdapter adapter;
    private RecyclerView rv;
    private View layoutEmpty;
    private TextView btnClearAll;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_deleted_passwords);
        db = DatabaseHelper.getInstance(this);

        findViewById(R.id.btn_back).setOnClickListener(v -> finish());

        rv = findViewById(R.id.rv_deleted);
        layoutEmpty = findViewById(R.id.layout_empty);
        btnClearAll = findViewById(R.id.btn_clear_all);

        adapter = new DeletedPasswordAdapter(new DeletedPasswordAdapter.Listener() {
            @Override
            public void onRestore(Password p) {
                db.restore(p.getId());
                load();
            }
            @Override
            public void onDeleteForever(Password p) {
                new AlertDialog.Builder(DeletedPasswordsActivity.this, R.style.AlertDialogDark)
                    .setTitle("Удалить навсегда?")
                    .setMessage("Это действие необратимо")
                    .setPositiveButton("Удалить", (d, w) -> { db.deletePermanently(p.getId()); load(); })
                    .setNegativeButton("Отмена", null)
                    .show();
            }
        });

        rv.setLayoutManager(new LinearLayoutManager(this));
        rv.setAdapter(adapter);

        btnClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this, R.style.AlertDialogDark)
                .setTitle("Очистить корзину?")
                .setMessage("Все удалённые пароли будут уничтожены навсегда")
                .setPositiveButton("Очистить", (d, w) -> { db.clearDeleted(); load(); })
                .setNegativeButton("Отмена", null)
                .show();
        });

        load();
    }

    private void load() {
        java.util.List<Password> list = db.getDeleted();
        adapter.setPasswords(list);
        boolean empty = list.isEmpty();
        rv.setVisibility(empty ? View.GONE : View.VISIBLE);
        layoutEmpty.setVisibility(empty ? View.VISIBLE : View.GONE);
        btnClearAll.setVisibility(empty ? View.GONE : View.VISIBLE);
    }
}
