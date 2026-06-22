package ru.moyparol.app.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.List;
import ru.moyparol.app.models.Password;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "moyparol.db";
    private static final int DB_VERSION = 1;
    private static final String TABLE = "passwords";
    private static final String COL_ID = "id";
    private static final String COL_SITE = "site_name";
    private static final String COL_LOGIN = "login";
    private static final String COL_PASSWORD = "password";
    private static final String COL_WEBSITE = "website";
    private static final String COL_NOTES = "notes";
    private static final String COL_CATEGORY = "category";
    private static final String COL_CREATED = "created_at";
    private static final String COL_UPDATED = "updated_at";
    private static final String COL_DELETED = "deleted";
    private static final String COL_DELETED_AT = "deleted_at";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) instance = new DatabaseHelper(context.getApplicationContext());
        return instance;
    }

    private DatabaseHelper(Context context) { super(context, DB_NAME, null, DB_VERSION); }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE + " (" +
                COL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_SITE + " TEXT NOT NULL, " +
                COL_LOGIN + " TEXT, " +
                COL_PASSWORD + " TEXT, " +
                COL_WEBSITE + " TEXT, " +
                COL_NOTES + " TEXT, " +
                COL_CATEGORY + " TEXT DEFAULT 'Другое', " +
                COL_CREATED + " INTEGER, " +
                COL_UPDATED + " INTEGER, " +
                COL_DELETED + " INTEGER DEFAULT 0, " +
                COL_DELETED_AT + " INTEGER DEFAULT 0)");
        insertSample(db, "Google", "user@gmail.com", "G00gleP@ss!23", "accounts.google.com", "Финансы");
        insertSample(db, "Apple", "user@icloud.com", "AppleSecret99!", "apple.com", "Другое");
        insertSample(db, "GitHub", "username", "GithubDev2024", "github.com", "Работа");
        insertSample(db, "Сбербанк", "user@sber.ru", "Sber1234", "sberbank.ru", "Финансы");
        insertSample(db, "Steam", "steamuser", "Steam@pass2024!", "store.steampowered.com", "Развлечения");
        insertSample(db, "ВКонтакте", "vk@mail.ru", "Vk2024Strong!", "vk.com", "Социальные сети");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE);
        onCreate(db);
    }

    private void insertSample(SQLiteDatabase db, String site, String login, String pass, String url, String cat) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SITE, site); cv.put(COL_LOGIN, login); cv.put(COL_PASSWORD, pass);
        cv.put(COL_WEBSITE, url); cv.put(COL_CATEGORY, cat);
        cv.put(COL_CREATED, System.currentTimeMillis()); cv.put(COL_UPDATED, System.currentTimeMillis());
        cv.put(COL_DELETED, 0); db.insert(TABLE, null, cv);
    }

    public long insertPassword(Password p) { return getWritableDatabase().insert(TABLE, null, toCV(p)); }

    public boolean updatePassword(Password p) {
        ContentValues cv = toCV(p); cv.put(COL_UPDATED, System.currentTimeMillis());
        return getWritableDatabase().update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(p.getId())}) > 0;
    }

    public boolean softDelete(long id) {
        ContentValues cv = new ContentValues();
        cv.put(COL_DELETED, 1); cv.put(COL_DELETED_AT, System.currentTimeMillis());
        return getWritableDatabase().update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean restore(long id) {
        ContentValues cv = new ContentValues(); cv.put(COL_DELETED, 0); cv.put(COL_DELETED_AT, 0);
        return getWritableDatabase().update(TABLE, cv, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public boolean deletePermanently(long id) {
        return getWritableDatabase().delete(TABLE, COL_ID + "=?", new String[]{String.valueOf(id)}) > 0;
    }

    public void clearDeleted() { getWritableDatabase().delete(TABLE, COL_DELETED + "=1", null); }

    public List<Password> getAllActive() { return query(COL_DELETED + "=0", null, COL_SITE + " ASC"); }

    public List<Password> search(String q) {
        String like = "%" + q + "%";
        return query(COL_DELETED + "=0 AND (" + COL_SITE + " LIKE ? OR " + COL_LOGIN + " LIKE ? OR " + COL_WEBSITE + " LIKE ?)",
                new String[]{like, like, like}, COL_SITE + " ASC");
    }

    public List<Password> getDeleted() { return query(COL_DELETED + "=1", null, COL_DELETED_AT + " DESC"); }

    public List<Password> getByCategory(String category) {
        return query(COL_DELETED + "=0 AND " + COL_CATEGORY + "=?", new String[]{category}, COL_SITE + " ASC");
    }

    public Password getById(long id) {
        List<Password> list = query(COL_ID + "=?", new String[]{String.valueOf(id)}, null);
        return list.isEmpty() ? null : list.get(0);
    }

    public int countActive() { return getAllActive().size(); }
    public int countDeleted() { return getDeleted().size(); }

    public int countWeak() {
        int count = 0;
        for (Password p : getAllActive()) { if (p.isWeak()) count++; }
        return count;
    }

    private List<Password> query(String where, String[] args, String order) {
        List<Password> list = new ArrayList<>();
        Cursor c = getReadableDatabase().query(TABLE, null, where, args, null, null, order);
        if (c != null) { while (c.moveToNext()) { list.add(fromCursor(c)); } c.close(); }
        return list;
    }

    private Password fromCursor(Cursor c) {
        Password p = new Password();
        p.setId(c.getLong(c.getColumnIndexOrThrow(COL_ID)));
        p.setSiteName(c.getString(c.getColumnIndexOrThrow(COL_SITE)));
        p.setLogin(c.getString(c.getColumnIndexOrThrow(COL_LOGIN)));
        p.setPassword(c.getString(c.getColumnIndexOrThrow(COL_PASSWORD)));
        p.setWebsite(c.getString(c.getColumnIndexOrThrow(COL_WEBSITE)));
        p.setNotes(c.getString(c.getColumnIndexOrThrow(COL_NOTES)));
        p.setCategory(c.getString(c.getColumnIndexOrThrow(COL_CATEGORY)));
        p.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(COL_CREATED)));
        p.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(COL_UPDATED)));
        p.setDeleted(c.getInt(c.getColumnIndexOrThrow(COL_DELETED)) == 1);
        p.setDeletedAt(c.getLong(c.getColumnIndexOrThrow(COL_DELETED_AT)));
        return p;
    }

    private ContentValues toCV(Password p) {
        ContentValues cv = new ContentValues();
        cv.put(COL_SITE, p.getSiteName()); cv.put(COL_LOGIN, p.getLogin());
        cv.put(COL_PASSWORD, p.getPassword()); cv.put(COL_WEBSITE, p.getWebsite());
        cv.put(COL_NOTES, p.getNotes()); cv.put(COL_CATEGORY, p.getCategory());
        cv.put(COL_CREATED, p.getCreatedAt()); cv.put(COL_UPDATED, p.getUpdatedAt());
        cv.put(COL_DELETED, p.isDeleted() ? 1 : 0); cv.put(COL_DELETED_AT, p.getDeletedAt());
        return cv;
    }
}
