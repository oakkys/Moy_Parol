package ru.moyparol.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import ru.moyparol.app.R;
import ru.moyparol.app.models.Password;

public class DeletedPasswordAdapter extends RecyclerView.Adapter<DeletedPasswordAdapter.VH> {

    public interface Listener {
        void onRestore(Password p);
        void onDeleteForever(Password p);
    }

    private List<Password> items = new ArrayList<>();
    private Listener listener;

    public DeletedPasswordAdapter(Listener listener) { this.listener = listener; }

    public void setPasswords(List<Password> list) {
        items.clear();
        if (list != null) items.addAll(list);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_deleted_password, parent, false);
        return new VH(v);
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int position) {
        Password p = items.get(position);
        h.tvIcon.setText(p.getIconLetter());
        h.tvSite.setText(p.getSiteName());
        long days = p.getDaysUntilDeletion();
        h.tvDays.setText("Удалится через " + days + " " + dayWord(days));
        h.btnRestore.setOnClickListener(v -> listener.onRestore(p));
        h.btnDelete.setOnClickListener(v -> listener.onDeleteForever(p));
    }

    @Override
    public int getItemCount() { return items.size(); }

    private String dayWord(long n) {
        long mod10 = n % 10, mod100 = n % 100;
        if (mod100 >= 11 && mod100 <= 14) return "дней";
        if (mod10 == 1) return "день";
        if (mod10 >= 2 && mod10 <= 4) return "дня";
        return "дней";
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvSite, tvDays, btnRestore, btnDelete;
        VH(View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tv_icon);
            tvSite = v.findViewById(R.id.tv_site_name);
            tvDays = v.findViewById(R.id.tv_days_left);
            btnRestore = v.findViewById(R.id.btn_restore);
            btnDelete = v.findViewById(R.id.btn_delete_forever);
        }
    }
}
