
package com.example.financialrecordmanagement;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;

import java.security.SecureRandom;
import java.security.spec.KeySpec;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

public final class PinSecurity {

    private static final String PREFS = "financial_security";
    private static final String PIN_HASH = "pin_hash";
    private static final String PIN_SALT = "pin_salt";
    private static final String USER_NAME = "user_name";

    private static final int ITERATIONS = 120000;
    private static final int KEY_LENGTH = 256;

    private PinSecurity() {}

    private static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(
                PREFS, Context.MODE_PRIVATE
        );
    }

    public static boolean hasPin(Context context) {
        return prefs(context).contains(PIN_HASH)
                && prefs(context).contains(PIN_SALT);
    }

    public static void saveUserName(Context context, String name) {
        prefs(context).edit()
                .putString(USER_NAME, name)
                .apply();
    }

    public static String getUserName(Context context) {
        return prefs(context).getString(USER_NAME, "User");
    }

    public static boolean savePin(Context context, String pin) {
        try {
            byte[] salt = new byte[16];
            new SecureRandom().nextBytes(salt);

            String hash = hashPin(pin, salt);

            prefs(context).edit()
                    .putString(PIN_HASH, hash)
                    .putString(
                            PIN_SALT,
                            Base64.encodeToString(salt, Base64.NO_WRAP)
                    )
                    .apply();

            return true;

        } catch (Exception e) {
            return false;
        }
    }

    public static boolean verifyPin(Context context, String pin) {
        try {
            SharedPreferences preferences = prefs(context);

            String savedHash = preferences.getString(PIN_HASH, null);
            String savedSalt = preferences.getString(PIN_SALT, null);

            if (savedHash == null || savedSalt == null) {
                return false;
            }

            byte[] salt = Base64.decode(savedSalt, Base64.NO_WRAP);
            String enteredHash = hashPin(pin, salt);

            return java.security.MessageDigest.isEqual(
                    savedHash.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                    enteredHash.getBytes(java.nio.charset.StandardCharsets.UTF_8)
            );

        } catch (Exception e) {
            return false;
        }
    }

    private static String hashPin(String pin, byte[] salt)
            throws Exception {

        KeySpec spec = new PBEKeySpec(
                pin.toCharArray(),
                salt,
                ITERATIONS,
                KEY_LENGTH
        );

        SecretKeyFactory factory =
                SecretKeyFactory.getInstance("PBKDF2WithHmacSHA1");

        byte[] hash = factory.generateSecret(spec).getEncoded();

        return Base64.encodeToString(hash, Base64.NO_WRAP);
    }
}
