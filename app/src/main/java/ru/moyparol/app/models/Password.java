package ru.moyparol.app.models;

public class Password {
    private long id;
    private String siteName;
    private String login;
    private String password;
    private String website;
    private String notes;
    private String category;
    private long createdAt;
    private long updatedAt;
    private boolean deleted;
    private long deletedAt;

    public Password() {
        this.createdAt = System.currentTimeMillis();
        this.updatedAt = System.currentTimeMillis();
        this.deleted = false;
        this.category = "Другое";
    }

    public Password(String siteName, String login, String password, String website) {
        this();
        this.siteName = siteName;
        this.login = login;
        this.password = password;
        this.website = website;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }
    public String getSiteName() { return siteName; }
    public void setSiteName(String siteName) { this.siteName = siteName; }
    public String getLogin() { return login; }
    public void setLogin(String login) { this.login = login; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public String getWebsite() { return website; }
    public void setWebsite(String website) { this.website = website; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public long getCreatedAt() { return createdAt; }
    public void setCreatedAt(long createdAt) { this.createdAt = createdAt; }
    public long getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(long updatedAt) { this.updatedAt = updatedAt; }
    public boolean isDeleted() { return deleted; }
    public void setDeleted(boolean deleted) { this.deleted = deleted; }
    public long getDeletedAt() { return deletedAt; }
    public void setDeletedAt(long deletedAt) { this.deletedAt = deletedAt; }

    public String getIconLetter() {
        if (siteName != null && !siteName.isEmpty()) {
            return String.valueOf(siteName.charAt(0)).toUpperCase();
        }
        return "?";
    }

    public long getDaysUntilDeletion() {
        if (!deleted || deletedAt == 0) return 30;
        long msLeft = (deletedAt + 30L * 24 * 60 * 60 * 1000) - System.currentTimeMillis();
        return Math.max(0, msLeft / (24 * 60 * 60 * 1000));
    }

    public int getStrengthScore() {
        if (password == null || password.length() < 6) return 0;
        int score = 0;
        if (password.length() >= 8) score++;
        if (password.length() >= 12) score++;
        if (password.matches(".*[A-Z].*")) score++;
        if (password.matches(".*[a-z].*")) score++;
        if (password.matches(".*[0-9].*")) score++;
        if (password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*")) score++;
        return score;
    }

    public boolean isWeak() { return getStrengthScore() <= 2; }
}
