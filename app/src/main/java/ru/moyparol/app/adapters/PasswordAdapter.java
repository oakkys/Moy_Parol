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

public class PasswordAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_HEADER = 0;
    private static final int TYPE_ITEM = 1;

    public interface OnItemClickListener {
        void onItemClick(Password password);
    }

    private List<Object> items = new ArrayList<>(); // String headers + Password items
    private OnItemClickListener listener;

    public PasswordAdapter(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setPasswords(List<Password> passwords) {
        items.clear();
        if (passwords == null || passwords.isEmpty()) {
            notifyDataSetChanged();
            return;
        }
        String lastHeader = null;
        for (Password p : passwords) {
            String letter = p.getIconLetter();
            if (!letter.equals(lastHeader)) {
                items.add(letter);
                lastHeader = letter;
            }
            items.add(p);
        }
        notifyDataSetChanged();
    }

    public void setPasswordsFlat(List<Password> passwords) {
        items.clear();
        if (passwords != null) items.addAll(passwords);
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        return items.get(position) instanceof String ? TYPE_HEADER : TYPE_ITEM;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inf = LayoutInflater.from(parent.getContext());
        if (viewType == TYPE_HEADER) {
            View v = inf.inflate(R.layout.item_section_header, parent, false);
            return new HeaderVH(v);
        } else {
            View v = inf.inflate(R.layout.item_password, parent, false);
            return new ItemVH(v);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderVH) {
            ((HeaderVH) holder).bind((String) items.get(position));
        } else {
            Password p = (Password) items.get(position);
            ((ItemVH) holder).bind(p);
            holder.itemView.setOnClickListener(v -> listener.onItemClick(p));
        }
    }

    @Override
    public int getItemCount() { return items.size(); }

    static class HeaderVH extends RecyclerView.ViewHolder {
        TextView tv;
        HeaderVH(View v) { super(v); tv = (TextView) v; }
        void bind(String letter) { tv.setText(letter); }
    }

    static class ItemVH extends RecyclerView.ViewHolder {
        TextView tvIcon, tvSite, tvLogin;
        ItemVH(View v) {
            super(v);
            tvIcon = v.findViewById(R.id.tv_icon);
            tvSite = v.findViewById(R.id.tv_site_name);
            tvLogin = v.findViewById(R.id.tv_login);
        }
        void bind(Password p) {
            tvIcon.setText(p.getIconLetter());
            tvSite.setText(p.getSiteName());
            tvLogin.setText(p.getLogin() != null ? p.getLogin() : p.getWebsite());
        }
    }
}
