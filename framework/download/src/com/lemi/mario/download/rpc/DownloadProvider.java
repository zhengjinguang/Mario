package com.lemi.mario.download.rpc;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.UriMatcher;
import android.database.CrossProcessCursor;
import android.database.Cursor;
import android.database.CursorWindow;
import android.database.CursorWrapper;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Binder;
import android.util.Log;

import java.util.HashSet;

/**
 * Not extends from ContentProvider because we don't need ContentProvider's features.
 * But we still use interface the same as ContentProvider. Using Uri to parse data and instruction
 * is a good practice.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class DownloadProvider {

  /** Database Name **/
  private static final String DB_NAME = "downloads.db";
  /** Current database Version **/
  private static final int DB_VERSION = 1;
  /** Old database Version **/
  private static final int OLD_MAX_DATABASE_VERSION = 1;
  /** Name of table in the database **/
  private static final String TABLE_NAME = "downloads";

  private static final UriMatcher uriMatcher = new UriMatcher(
      UriMatcher.NO_MATCH);

  private static class MatchType {
    /** all downloads ex. downloads/ **/
    public static final int DOWNLOADS = 1;
    /** download Id ex. downloads/3 **/
    public static final int DOWNLOADS_ID = 2;
  }

  /** MIME type for the entire download list */
  private static final String DOWNLOAD_LIST_TYPE = "vnd.android.cursor.dir/download";
  /** MIME type for an individual download */
  private static final String DOWNLOAD_TYPE = "vnd.android.cursor.item/download";

  static {
    uriMatcher.addURI(DownloadConstants.AUTHORITY,
        DownloadConstants.DOWNLOAD_TABLE, MatchType.DOWNLOADS);
    uriMatcher
        .addURI(DownloadConstants.AUTHORITY,
            DownloadConstants.DOWNLOAD_TABLE + "/#",
            MatchType.DOWNLOADS_ID);
  }

  private static final String[] appReadableColumnsArray = new String[] {
      DownloadConstants.Database.COLUMNS.COLUMN_ID,
      DownloadConstants.Database.COLUMNS.COLUMN_URI,
      DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES,
      DownloadConstants.Database.COLUMNS.COLUMN_DESCRIPTION,
      DownloadConstants.Database.COLUMNS.COLUMN_DESTINATION,
      DownloadConstants.Database.COLUMNS.COLUMN_FOLDER_PATH,
      DownloadConstants.Database.COLUMNS.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI,
      DownloadConstants.Database.COLUMNS.COLUMN_LAST_MODIFICATION,
      DownloadConstants.Database.COLUMNS.COLUMN_MIME_TYPE,
      DownloadConstants.Database.COLUMNS.COLUMN_FILE_PATH,
      DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_CLASS,
      DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_EXTRAS,
      DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_TYPE,
      DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_EXTRAS,
      DownloadConstants.Database.COLUMNS.COLUMN_STATUS,
      DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY,
      DownloadConstants.Database.COLUMNS.COLUMN_NO_INTEGRITY,
      DownloadConstants.Database.COLUMNS.COLUMN_TITLE,
      DownloadConstants.Database.COLUMNS.COLUMN_FAILED_TIMES,
      DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES,
      DownloadConstants.Database.COLUMNS.COLUMN_USE_AGENT,
      DownloadConstants.Database.COLUMNS.COLUMN_ETAG,
      DownloadConstants.Database.COLUMNS.COLUMN_SOURCE,
      DownloadConstants.Database.COLUMNS.COLUMN_CHECKSIZE,
      DownloadConstants.Database.COLUMNS.COLUMN_ICON_URL,
      DownloadConstants.Database.COLUMNS.COLUMN_DURATION,
      DownloadConstants.Database.COLUMNS.COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI,
      DownloadConstants.Database.COLUMNS.COLUMN_RETRIED_URLS,
      DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
      DownloadConstants.Database.COLUMNS.COLUMN_LAST_URL_RETRIED_TIMES,
      DownloadConstants.Database.COLUMNS.COLUMN_MD5_CHECKSUM,
      DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE,
      DownloadConstants.Database.COLUMNS.COLUMN_LAST_URL_RETRIED_TIMES,
      DownloadConstants.Database.COLUMNS.COLUMN_SPEED_LIMIT,
      DownloadConstants.Database.COLUMNS.COLUMN_SPEED,
      DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_TYPE,
      DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_VALUE,
  };

  private static HashSet<String> appReadableColumnsSet;
  static {
    appReadableColumnsSet = new HashSet<String>();
    for (int i = 0; i < appReadableColumnsArray.length; ++i) {
      appReadableColumnsSet.add(appReadableColumnsArray[i]);
    }
  }

  private SQLiteOpenHelper databaseHelper = null;
  private Context context;

  private static final class DatabaseHelper extends SQLiteOpenHelper {

    public DatabaseHelper(final Context context) {
      super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
      if (DownloadConstants.DEBUG) {
        Log.d(DownloadConstants.TAG, "create database");
      }
      onUpgrade(db, 0, DB_VERSION);
    }

    @Override
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      resetDatabase(db);
    }

    /**
     * drop new version database and rebuild one according to the database verison.
     * 
     * @param db
     */
    private void resetDatabase(SQLiteDatabase db) {
      db.execSQL("DROP TABLE IF EXISTS " + DownloadConstants.DOWNLOAD_TABLE);
      onCreate(db);
    }

    /**
     * Upgrade database from version 117 to version
     * 
     * @param db
     * @param version
     */
    private static void upgradeTo(SQLiteDatabase db, int version) {
      switch (version) {
        default:
          break;
      }
    }

    /**
     * Add a column to a table using ALTER TABLE.
     * 
     * @param dbTable name of the table
     * @param columnName name of the column to add
     * @param columnDefinition SQL for the column definition
     */
    private static void addColumn(SQLiteDatabase db, String dbTable, String columnName,
        String columnDefinition) {
      db.execSQL("ALTER TABLE " + dbTable + " ADD COLUMN " + columnName + " "
          + columnDefinition);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
      // do any upgrade
      if (oldVersion <= OLD_MAX_DATABASE_VERSION) {
        // if the old database is existed, remove it from database
        createDatabase(db);
      }
      // upgrade database
      for (int i = oldVersion + 1; i <= newVersion; i++) {
        upgradeTo(db, i);
      }
    }

    private static void createDatabase(SQLiteDatabase db) {
      try {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
        db.execSQL("CREATE TABLE "
            + TABLE_NAME
            + "("
            + DownloadConstants.Database.COLUMNS.COLUMN_ID
            + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + DownloadConstants.Database.COLUMNS.COLUMN_URI
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_DESCRIPTION
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_FOLDER_PATH
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_DESTINATION
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI
            + " INTEGER NOT NULL DEFAULT 1, "
            + DownloadConstants.Database.COLUMNS.COLUMN_LAST_MODIFICATION
            + " BIGINT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_MIME_TYPE
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_FILE_PATH
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_EXTRAS
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_TYPE
            + " INTEGERT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_STATUS
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_TITLE
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_USE_AGENT
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI
            + " BOOLEAN, "
            + DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_CLASS
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_ETAG
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_NO_INTEGRITY
            + " BOOLEAN, "
            + DownloadConstants.Database.COLUMNS.COLUMN_SOURCE
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_CHECKSIZE
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_ICON_URL
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_EXTRAS
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_DURATION
            + " INTEGER NOT NULL DEFAULT 0, "
            + DownloadConstants.Database.COLUMNS.COLUMN_RETRIED_URLS
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_MD5_CHECKSUM
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_TYPE
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_VALUE
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE
            + " TEXT, "
            + DownloadConstants.Database.COLUMNS.COLUMN_SPEED_LIMIT
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_SPEED
            + " INTEGER, "
            + DownloadConstants.Database.COLUMNS.COLUMN_FAILED_TIMES
            + " INTEGER NOT NULL DEFAULT 0, "
            + DownloadConstants.Database.COLUMNS.COLUMN_LAST_URL_RETRIED_TIMES
            + " INTEGER NOT NULL DEFAULT 0);"
            );
      } catch (SQLException ex) {
        ex.printStackTrace();
        throw ex;
      }
    }

  }

  private static DownloadProvider instance;

  public static synchronized DownloadProvider getInstance(Context context) {
    if (instance == null) {
      instance = new DownloadProvider(context);
    }
    return instance;
  }

  private DownloadProvider(Context context) {
    this.context = context;
    databaseHelper = new DatabaseHelper(context);
  }

  public int delete(Uri uri, String selection, String[] selectionArgs) {
    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    int match = uriMatcher.match(uri);
    int count = 0;
    switch (match) {
      case MatchType.DOWNLOADS:
      case MatchType.DOWNLOADS_ID:
        SqlSelection sqlselection = getWhereClause(uri, selection,
            selectionArgs, match);
        try {
          count = db.delete(TABLE_NAME, sqlselection.getSelection(),
              sqlselection.getParameters());
        } catch (SQLException e) {
          e.printStackTrace();
          count = 0;
        }
        break;
      default:
        throw new UnsupportedOperationException("Cannot delete URI: " + uri);
    }
    return count;
  }

  public String getType(Uri uri) {
    int match = uriMatcher.match(uri);
    switch (match) {
      case MatchType.DOWNLOADS:
        return DOWNLOAD_LIST_TYPE;
      case MatchType.DOWNLOADS_ID:
        return DOWNLOAD_TYPE;
      default:
        throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
    }
  }

  public Uri insert(Uri uri, ContentValues values) {
    SQLiteDatabase database = databaseHelper.getWritableDatabase();
    int match = uriMatcher.match(uri);
    if (match != MatchType.DOWNLOADS) {
      throw new IllegalArgumentException("Unknown/Invalid URI " + uri);
    }
    ContentValues to = new ContentValues();
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_URI, values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_FOLDER_PATH,
        values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_MIME_TYPE, values,
        to);
    copyBoolean(DownloadConstants.Database.COLUMNS.COLUMN_NO_INTEGRITY, values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_FILE_PATH, values, to);
    // ensure has write external storage permission
    copyBoolean(DownloadConstants.Database.COLUMNS.COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI, values, to);
    context.enforcePermission(
        android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
        Binder.getCallingPid(), Binder.getCallingUid(),
        "need WRITE_EXTERNAL_STORAGE permission to use DESTINATION_FILE_URI");

    copyInteger(DownloadConstants.Database.COLUMNS.COLUMN_DESTINATION,
        values, to);

    copyInteger(DownloadConstants.Database.COLUMNS.COLUMN_STATUS,
        values, to);

    copyBoolean(
        DownloadConstants.Database.COLUMNS.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI,
        values, to);

    copyString(DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY,
        values, to);

    copyString(
        DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_CLASS,
        values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG,
        values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_MD5_CHECKSUM,
        values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE,
        values, to);
    copyString(
        DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_EXTRAS,
        values, to);

    // source String
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_SOURCE, values, to);

    copyInteger(DownloadConstants.Database.COLUMNS.COLUMN_CHECKSIZE, values, to);
    // duration
    copyInteger(DownloadConstants.Database.COLUMNS.COLUMN_DURATION, values, to);
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_ICON_URL, values, to);
    // agent
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_USE_AGENT, values,
        to);

    copyStringWithDefault(DownloadConstants.Database.COLUMNS.COLUMN_TITLE,
        values, to, "");
    copyStringWithDefault(
        DownloadConstants.Database.COLUMNS.COLUMN_DESCRIPTION, values,
        to, "");
    copyString(DownloadConstants.Database.COLUMNS.COLUMN_RETRIED_URLS, values, to);
    copyInteger(DownloadConstants.Database.COLUMNS.COLUMN_LAST_URL_RETRIED_TIMES, values, to);
    copyLong(DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES, values, to);
    to.put(DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES, 0);

    // resource type eg. apk/video/musice
    copyInteger(DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_TYPE,
        values, to);
    copyStringWithDefault(
        DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_EXTRAS,
        values, to, "");
    copyLong(DownloadConstants.Database.COLUMNS.COLUMN_SPEED_LIMIT, values, to);
    copyLong(DownloadConstants.Database.COLUMNS.COLUMN_SPEED, values, to);
    copyStringWithDefault(DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_TYPE, values, to, "");
    copyStringWithDefault(DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_VALUE, values, to, "");

    assert database != null;
    long id = -1;
    try {
      id = database.insert(TABLE_NAME, null, to);
    } catch (SQLException e) {
      e.printStackTrace();
    }
    if (id == -1) {
      if (DownloadConstants.DEBUG) {
        Log.d(DownloadConstants.TAG, "insert " + uri
            + " into database error!");
        return null;
      }
    }

    notifyContentChanged(uri, match);
    return ContentUris.withAppendedId(
        DownloadConstants.Database.CONTENT_URI, id);
  }

  public Cursor query(Uri uri, String[] projection, String where,
      String[] whereArgs, String sortOrder) {
    SQLiteDatabase db = databaseHelper.getReadableDatabase();

    /*
     * may also need to confirm same process
     */

    int match = uriMatcher.match(uri);
    if (match == -1) {
      throw new IllegalArgumentException("Unknown URI: " + uri);
    }

    SqlSelection fullSelection = getWhereClause(uri, where, whereArgs,
        match);

    if (projection == null) {
      projection = appReadableColumnsArray;
    } else {
      for (int i = 0; i < projection.length; ++i) {
        if (!appReadableColumnsSet.contains(projection[i])) {
          throw new IllegalArgumentException("column "
              + projection[i] + " is not allowed in queries");
        }
      }
    }

    Cursor ret = db.query(TABLE_NAME, projection, fullSelection
        .getSelection(), fullSelection.getParameters(), null, null,
        sortOrder);

    if (ret != null) {
      ret = new ReadOnlyCursorWrapper(ret);
    }

    if (ret != null) {
      ret.setNotificationUri(context.getContentResolver(), uri);
    }

    return ret;
  }

  public int update(Uri uri, ContentValues values, String selection,
      String[] selectionArgs) {

    /*
     * notice if start with different process, take care of it
     */

    SQLiteDatabase db = databaseHelper.getWritableDatabase();
    int count;
    int match = uriMatcher.match(uri);

    if (match == MatchType.DOWNLOADS || match == MatchType.DOWNLOADS_ID) {
      SqlSelection sqlselection = getWhereClause(uri, selection,
          selectionArgs, match);
      if (values.size() > 0) {
        try {
          count = db.update(TABLE_NAME, values, sqlselection
              .getSelection(), sqlselection.getParameters());
        } catch (SQLException e) {
          e.printStackTrace();
          count = 0;
        }
      } else {
        count = 0;
      }
    } else {
      throw new UnsupportedOperationException("Cannot update URI: " + uri);
    }
    notifyContentChanged(uri, match);
    return count;
  }

  private static final void copyInteger(String key, ContentValues from,
      ContentValues to) {
    Integer i = from.getAsInteger(key);
    if (i != null) {
      to.put(key, i);
    }
  }

  private static final void copyBoolean(String key, ContentValues from,
      ContentValues to) {
    Boolean b = from.getAsBoolean(key);
    if (b != null) {
      to.put(key, b);
    }
  }

  private static final void copyLong(String key, ContentValues from,
      ContentValues to) {
    Long l = from.getAsLong(key);
    if (l != null) {
      to.put(key, l);
    }
  }

  private static final void copyString(String key, ContentValues from,
      ContentValues to) {
    String s = from.getAsString(key);
    if (s != null) {
      to.put(key, s);
    }
  }

  private static final void copyStringWithDefault(String key,
      ContentValues from, ContentValues to, String defaultValue) {
    copyString(key, from, to);
    if (!to.containsKey(key)) {
      to.put(key, defaultValue);
    }
  }

  private void notifyContentChanged(final Uri uri, int uriMatch) {
    Long downloadId = null;
    if (uriMatch == MatchType.DOWNLOADS_ID) {
      downloadId = Long.parseLong(getDownloadIdFromUri(uri));
    }
    if (downloadId != null) {
      Uri uriToNotify = ContentUris.withAppendedId(
          DownloadConstants.Database.CONTENT_URI, downloadId);
      context.getContentResolver().notifyChange(uriToNotify, null);
    }
  }

  private SqlSelection getWhereClause(final Uri uri, final String where,
      final String[] whereArgs, int uriMatch) {
    SqlSelection selection = new SqlSelection();
    selection.appendClause(where, whereArgs);
    if (uriMatch == MatchType.DOWNLOADS_ID) {
      selection.appendClause(DownloadConstants.Database.COLUMNS.COLUMN_ID
          + " = ?", getDownloadIdFromUri(uri));
    }
    return selection;
  }

  private String getDownloadIdFromUri(final Uri uri) {
    return uri.getPathSegments().get(1);
  }

  private class ReadOnlyCursorWrapper extends CursorWrapper implements
      CrossProcessCursor {
    public ReadOnlyCursorWrapper(Cursor cursor) {
      super(cursor);
      mCursor = (CrossProcessCursor) cursor;
    }

    @Override
    public void fillWindow(int pos, CursorWindow window) {
      mCursor.fillWindow(pos, window);
    }

    @Override
    public CursorWindow getWindow() {
      return mCursor.getWindow();
    }

    @Override
    public boolean onMove(int oldPosition, int newPosition) {
      return mCursor.onMove(oldPosition, newPosition);
    }

    private final CrossProcessCursor mCursor;
  }
}
