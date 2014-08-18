package com.livenation.mobile.android.na.cash.service;

import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.livenation.mobile.android.na.app.LiveNationApplication;
import com.livenation.mobile.android.na.cash.model.CashUtils;
import com.livenation.mobile.android.na.cash.service.responses.CashSession;

import java.io.IOException;

public interface SessionPersistenceProvider {
    @Nullable CashSession loadSession();
    void saveSession(@Nullable CashSession session);

    public static class Preferences implements SessionPersistenceProvider {
        private static final String PERSISTED_SESSION = "PERSISTED_SESSION";

        private final SharedPreferences sharedPreferences;

        public Preferences() {
            this.sharedPreferences = LiveNationApplication.get().getSharedPreferences(CashUtils.PREFS_ID, 0);
        }

        @Nullable
        @Override
        public CashSession loadSession() {
            if (sharedPreferences.contains(PERSISTED_SESSION)) {
                try {
                    return CashSession.fromJsonString(sharedPreferences.getString(PERSISTED_SESSION, "{}"), CashSession.class);
                } catch (IOException e) {
                    Log.w(CashUtils.LOG_TAG, "Could not load session from persistent storage", e);
                }
            }
            return null;
        }

        @Override
        public void saveSession(@Nullable CashSession session) {
            SharedPreferences.Editor editor = sharedPreferences.edit();
            if (session != null) {
                try {
                    editor.putString(PERSISTED_SESSION, session.toJsonString());
                } catch (IOException e) {
                    Log.e(CashUtils.LOG_TAG, "Could not persist session", e);
                }
            } else {
                editor.remove(PERSISTED_SESSION);
            }
            editor.apply();
        }
    }
}
