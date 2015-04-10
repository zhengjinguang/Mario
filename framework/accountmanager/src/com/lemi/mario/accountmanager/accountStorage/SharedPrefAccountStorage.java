package com.lemi.mario.accountmanager.accountStorage;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.TextUtils;

import com.lemi.mario.accountmanager.config.UserKeys;
import com.lemi.mario.accountmanager.model.User;
import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.base.utils.SharePrefSubmitor;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class SharedPrefAccountStorage implements AccountStorage {
  private static final String PREFS_PATH = "mario-account-apps-prefs";
  private static SharedPreferences prefs;
  private Context appContext;

  public SharedPrefAccountStorage() {
    appContext = GlobalConfig.getAppContext();
  }

  private SharedPreferences getPrefs() {
    if (prefs == null) {
      prefs = appContext.getSharedPreferences(PREFS_PATH, Context.MODE_PRIVATE);
    }
    return prefs;
  }

  @Override
  public void saveAuth(String auth) {
    if (!TextUtils.isEmpty(auth)) {
      SharedPreferences.Editor editor = getPrefs().edit();
      editor.putString(UserKeys.AUTHCODE, auth);
      SharePrefSubmitor.submit(editor);
    }
  }

  @Override
  public void saveUserInfo(User user) {
    if (user == null) {
      return;
    }

    SharedPreferences.Editor editor = getPrefs().edit();
    if (!TextUtils.isEmpty(user.getNick())) {
      editor.putString(UserKeys.NICK, user.getNick());
    }
    editor.putInt(UserKeys.UID, user.getUid());
    if (!TextUtils.isEmpty(user.getUdid())) {
      editor.putString(UserKeys.UDID, user.getUdid());
    }
    if (!TextUtils.isEmpty(user.getEmail())) {
      editor.putString(UserKeys.EMAIL, user.getEmail());
    }
    if (!TextUtils.isEmpty(user.getPhone())) {
      editor.putString(UserKeys.PHONE, user.getPhone());
    }
    editor.putInt(UserKeys.GENDER, user.getGender());
    SharePrefSubmitor.submit(editor);

  }

  @Override
  public String getAuth() {
    return getPrefs().getString(UserKeys.AUTHCODE, "");
  }

  @Override
  public User getUserInfo() {
    SharedPreferences pref = getPrefs();
    if (!pref.contains(UserKeys.NICK)
        && !pref.contains(UserKeys.UID)
        && !pref.contains(UserKeys.UDID)
        && !pref.contains(UserKeys.EMAIL)
        && !pref.contains(UserKeys.PHONE)
        && !pref.contains(UserKeys.GENDER)) {
      return null;
    }
    User user = new User();
    user.setNick(pref.getString(UserKeys.NICK, ""));
    user.setUid(pref.getInt(UserKeys.UID, 0));
    user.setUdid(pref.getString(UserKeys.UDID, ""));
    user.setEmail(pref.getString(UserKeys.EMAIL, ""));
    user.setPhone(pref.getString(UserKeys.PHONE, ""));
    user.setGender(pref.getInt(UserKeys.GENDER, 0));
    return user;
  }

  @Override
  public void invalidateAuth() {
    SharedPreferences.Editor editor = getPrefs().edit();
    editor.remove(UserKeys.AUTHCODE);
    SharePrefSubmitor.submit(editor);
  }

}
