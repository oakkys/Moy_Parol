package ru.moyparol.app.utils;

import android.util.Base64;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

public class CryptoUtil {

    private static final String KEY = "MoyParol2024Key!"; // 16 bytes for AES-128
    private static final String ALGO = "AES";
    private static final String CIPHER = "AES/ECB/PKCS5Padding";

    public static String encrypt(String plain) {
        if (plain == null) return "";
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), ALGO);
            Cipher cipher = Cipher.getInstance(CIPHER);
            byte[] encrypted = cipher.doFinal(plain.getBytes("UTF-8"));
            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (Exception e) {
            return plain; // fallback
        }
    }

    public static String decrypt(String encrypted) {
        if (encrypted == null) return "";
        try {
            SecretKeySpec keySpec = new SecretKeySpec(KEY.getBytes("UTF-8"), ALGO);
            Cipher cipher = Cipher.getInstance(CIPHER);
            cipher.init(Cipher.DECRYPT_MODE, keySpec);
            byte[] decoded = Base64.decode(encrypted, Base64.DEFAULT);
            return new String(cipher.doFinal(decoded), "UTF-8");
        } catch (Exception e) {
            return encrypted; // fallback if not encrypted
        }
    }

    public static String generatePassword(int length, boolean upper, boolean digits, boolean symbols) {
        String lower = "abcdefghijklmnopqrstuvwxyz";
        String up = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        String nums = "0123456789";
        String sym = "!@#$%^&*()_+-=[]{}";

        StringBuilder chars = new StringBuilder(lower);
        if (upper) chars.append(up);
        if (digits) chars.append(nums);
        if (symbols) chars.append(sym);

        java.util.Random random = new java.security.SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
