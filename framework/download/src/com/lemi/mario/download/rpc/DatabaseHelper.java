package com.lemi.mario.download.rpc;

import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_CHECKSIZE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_CURRENT_BYTES;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_DESCRIPTION;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_DURATION;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_ETAG;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_FAILED_TIMES;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_FILE_PATH;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_FOLDER_PATH;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_ICON_URL;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_ID;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_LAST_MODIFICATION;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_LAST_URL_RETRIED_TIMES;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_MD5_CHECKSUM;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_MD5_STATE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_MIME_TYPE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_NOTIFICATION_CLASS;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_NO_INTEGRITY;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_EXTRAS;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_RESOURCE_TYPE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_RETRIED_URLS;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_SEGMENT_CONFIG;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_SOURCE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_SPEED;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_SPEED_LIMIT;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_STATUS;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_TITLE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_TOTAL_BYTES;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_URI;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_USE_AGENT;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_TYPE;
import static com.lemi.mario.download.rpc.DownloadConstants.Database.COLUMNS.COLUMN_VERIFY_VALUE;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.google.gson.Gson;
import com.lemi.mario.base.utils.FileNameUtil;
import com.lemi.mario.download.model.segments.DownloadConfigInfo;
import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;
import com.lemi.mario.download.rpc.InnerDownloadFilter.DownloadFilterBuilder.FilterItem;
import com.lemi.mario.download.rpc.InnerDownloadFilter.FilterArea;
import com.lemi.mario.download.utils.StorageUtil;
import com.twmacinta.util.MD5State;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database toolkit for database operations.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DatabaseHelper {

  private DatabaseHelper() {}

  private static final String SPLIT = ";";

  /**
   * Insert download request into database.
   * 
   * @param context
   * @param request
   * @return id in database
   */
  public static long insertItem(Context context, InnerDownloadRequest request) {
    ContentValues values = new ContentValues();
    assert (!TextUtils.isEmpty(request.url));
    values.put(COLUMN_URI, request.url);
    values.put(COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI, request.allowedInMobile);

    String folderPath;
    if (TextUtils.isEmpty(request.folderPath)) {
      // Determine file path.
      if (request.totalBytes > 0) {
        folderPath =
            StorageUtil.getExternalContentDirectory(request.type, request.totalBytes);
      } else {
        folderPath = StorageUtil.getExternalContentDirectory(request.type);
      }
    } else {
      folderPath = request.folderPath;
    }
    if (!TextUtils.isEmpty(folderPath) && !folderPath.endsWith("/")) {
      folderPath += "/";
    }
    putIfNonNull(values, COLUMN_FOLDER_PATH, folderPath);

    if (!TextUtils.isEmpty(folderPath) && !TextUtils.isEmpty(request.fileName)) {
      String fileName = FileNameUtil.removeIllegalChars(request.fileName);
      values.put(COLUMN_FILE_PATH, folderPath + fileName);
    }

    if (request.verifyType != null) {
      values.put(COLUMN_VERIFY_TYPE, request.verifyType.toString());
    }
    if (!TextUtils.isEmpty(request.verifyValue)) {
      values.put(COLUMN_VERIFY_VALUE, request.verifyValue);
    }

    // default true
    values.put(COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, request.visible);
    values.put(COLUMN_NO_INTEGRITY, true);
    if (request.extras != null) {
      values.put(COLUMN_RESOURCE_EXTRAS, request.extras);
    }
    values.put(COLUMN_RESOURCE_TYPE, request.type.ordinal());
    putIfNonNull(values, COLUMN_SOURCE, request.source);
    values.put(COLUMN_CURRENT_BYTES, 0L);
    values.put(COLUMN_TOTAL_BYTES, request.totalBytes > 0 ? request.totalBytes : 0);
    values.put(COLUMN_STATUS, DownloadConstants.Status.STATUS_CREATED);
    putIfNonNull(values, COLUMN_TITLE, request.title);
    putIfNonNull(values, COLUMN_DESCRIPTION, request.description);
    putIfNonNull(values, COLUMN_ICON_URL, request.iconUrl);
    values.put(COLUMN_CHECKSIZE, request.checkSize);
    values.put(COLUMN_IDENTITY, request.identity);
    values.put(COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI, request.allowedInMobile);

    values.put(COLUMN_SPEED_LIMIT, request.speedLimit);

    try {
      Uri uri = DownloadProvider.getInstance(context).insert(
          DownloadConstants.Database.CONTENT_URI, values);
      if (uri != null) {
        return ContentUris.parseId(uri);
      }
    } catch (Exception e) {
      e.printStackTrace();
    }

    return -1;
  }


  /**
   * update database with given content values.
   * 
   * @param context
   * @param id
   * @param values
   * @return affected row numbers
   */
  public static int updateDownloadInfo(Context context, long id, ContentValues values) {
    return DownloadProvider.getInstance(context).update(ContentUris.withAppendedId(
        DownloadConstants.Database.CONTENT_URI, id), values, null, null);
  }

  /**
   * Update downloadInfo into database.
   * 
   * <p>
   * When download a file, some detail info will be change, immediately sync to database
   * </p>
   * 
   * @param context
   * @param info
   * @return the row effected
   */
  public static int updateDownloadTaskInfo(Context context, InnerDownloadInfo info) {

    assert (context != null);

    ContentValues values = new ContentValues();
    values.put(COLUMN_TOTAL_BYTES, info.mTotalBytes);
    values.put(COLUMN_CURRENT_BYTES, info.mCurrentBytes);
    values.put(COLUMN_STATUS, info.getStatus());
    putIfNonNull(values, COLUMN_URI, info.mUri);
    putIfNonNull(values, COLUMN_DESCRIPTION, info.mDescription);
    putIfNonNull(values, COLUMN_FOLDER_PATH, info.mFolderPath);
    putIfNonNull(values, COLUMN_FILE_PATH, info.mFilePath);
    values.put(COLUMN_LAST_MODIFICATION, info.mLastMod);
    putIfNonNull(values, COLUMN_MIME_TYPE, info.mMimeType);
    putIfNonNull(values, COLUMN_TITLE, info.mTitle);
    putIfNonNull(values, COLUMN_ETAG, info.mETag);
    values.put(COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, info.mVisible);
    values.put(COLUMN_DURATION, info.mDuration);
    putIfNonNull(values, COLUMN_USE_AGENT, info.mUserAgent);
    putIfNonNull(values, COLUMN_RETRIED_URLS, info.mRetriedUrls);
    values.put(COLUMN_LAST_URL_RETRIED_TIMES, info.mLastUrlRetriedTimes);
    putIfNonNull(values, COLUMN_IDENTITY, info.mIdentity);
    putIfNonNull(values, COLUMN_SOURCE, info.mSource);
    putIfNonNull(values, COLUMN_DURATION, info.mDuration);
    putIfNonNull(values, COLUMN_ICON_URL, info.mIconUrl);
    values.put(COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI, info.mAllowInMobile);
    values.put(COLUMN_FAILED_TIMES, info.mNumFailed);
    putIfNonNull(values, COLUMN_RESOURCE_EXTRAS, info.mExtras);
    putIfNonNull(values, COLUMN_RETRIED_URLS, info.mRetriedUrls);
    values.put(COLUMN_LAST_URL_RETRIED_TIMES, info.mLastUrlRetriedTimes);
    putIfNonNull(values, COLUMN_MD5_CHECKSUM, info.mMd5);
    if (info.md5State != null) {
      putIfNonNull(values, COLUMN_MD5_STATE, new Gson().toJson(info.md5State));
    }
    values.put(COLUMN_SPEED_LIMIT, info.speedLimit);
    values.put(COLUMN_SPEED, info.getSpeed());

    // added to sync config infos in database.
    if (info.mConfig != null) {
      putIfNonNull(values, COLUMN_SEGMENT_CONFIG, info.mConfig.toJson());
    }
    return DownloadProvider.getInstance(context).update(ContentUris.withAppendedId(
        DownloadConstants.Database.CONTENT_URI, info.mId), values, null, null);
  }

  private static String generateSortOrder(int offset,
      int limit, FilterArea ordercolumn, boolean asc) {
    StringBuilder builder = new StringBuilder();
    if (limit != 0 && offset != 0 && ordercolumn != null) {
      if (asc) {
        builder.append(ordercolumn.getColumnName()).append(" ASC LIMIT ").append(limit)
            .append(" offset ").append(offset);
      } else {
        builder.append(ordercolumn.getColumnName()).append(" DESC LIMIT ").append(limit)
            .append(" offset ").append(offset);
      }
    }
    return builder.toString();
  }

  private static String generateSelection(InnerDownloadFilter filter) {
    StringBuilder builder = new StringBuilder();
    int num = 0;
    if (filter != null) {
      for (FilterItem item : filter.getFilters()) {
        String column = item.columnName.getColumnName();
        num++;
        if (num == 1) {
          builder.append(" (").append(column);
        } else {
          builder.append(" AND (").append(column);
        }
        switch (item.type) {
          case EQUAL:
            List<String> values = item.value;
            for (int i = 0; i < values.size(); i++) {
              if (i == 0) {
                builder.append(" = ").append(values.get(i));
              } else {
                builder.append(" OR ").append(column).append(" = ").append(values.get(i));
              }
            }
            break;
          case MORE:
            builder.append(" > ").append(item.value.get(0));
            break;
          case LESS:
            builder.append(" < ").append(item.value.get(0));
            break;
          case LIKE:
            List<String> regexList = item.value;
            for (int i = 0; i < regexList.size(); i++) {
              if (i == 0) {
                builder.append(" LIKE ").append(regexList.get(i));
              } else {
                builder.append(" AND ").append(column).append(" LIKE ").append(regexList.get(i));
              }
            }
            break;
          default:
            break;
        }
        builder.append(')');
      }
    }
    return builder.toString();
  }

  /* If filter is null, it means fetching all data in databases */
  public static List<InnerDownloadInfo> getDownloadInfos(Context context,
      InnerDownloadFilter filter,
      int offset,
      int limit, FilterArea ordercolumn, boolean asc) {
    String sortOrder = generateSortOrder(offset, limit, ordercolumn, asc);
    String selection = generateSelection(filter);
    List<InnerDownloadInfo> downloadInfos = new ArrayList<InnerDownloadInfo>();

    Cursor cursor = null;
    try {
      cursor = DownloadProvider.getInstance(context).query(DownloadConstants.Database.CONTENT_URI,
          null, selection, null, sortOrder);
      if (cursor != null) {
        while (cursor.moveToNext()) {
          try {
            // try at internal, because we don't want break looper when one data is illegal
            InnerDownloadInfo info = readFromDatabase(cursor);
            downloadInfos.add(info);
          } catch (Exception e) {
            e.printStackTrace();
          }
        }
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return downloadInfos;
  }

  public static int getDownloadCount(Context context, InnerDownloadFilter filter) {
    String selection = generateSelection(filter);
    Cursor cursor = null;
    try {
      cursor = DownloadProvider.getInstance(context).query(DownloadConstants.Database.CONTENT_URI,
          null, selection, null, null);
      return cursor.getCount();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return 0;
  }

  public static int updateItem(Context context, long id, long currentBytes) {
    ContentValues values = new ContentValues();
    values.put(COLUMN_STATUS, DownloadConstants.Status.STATUS_RUNNING);
    values.put(COLUMN_CURRENT_BYTES, currentBytes);
    return DownloadProvider.getInstance(context).update(ContentUris.withAppendedId(
        DownloadConstants.Database.CONTENT_URI, id), values, null, null);
  }

  /**
   * delete item from database.
   * 
   * @param context
   * @param id
   * @return the num of rows which have been effected.
   */
  public static int deleteItem(Context context, long id) {
    return DownloadProvider.getInstance(context).delete(ContentUris.withAppendedId(
        DownloadConstants.Database.CONTENT_URI, id), null, null);
  }

  public static InnerDownloadInfo getDownloadInfoFromId(Context context, Long id) {
    Cursor cursor = DownloadProvider.getInstance(context).query(
        DownloadConstants.Database.CONTENT_URI,
        null, COLUMN_ID + "= ?", new String[] {Long.toString(id)}, null);
    try {
      if (cursor.moveToFirst()) {
        return readFromDatabase(cursor);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return null;
  }

  public static List<InnerDownloadInfo> getDownloadInfosFromIds(Context context, List<Long> ids) {
    List<InnerDownloadInfo> infos = new ArrayList<InnerDownloadInfo>();
    StringBuilder builder = new StringBuilder();
    int i = 0;
    for (Long id : ids) {
      if (i != 0) {
        builder.append(" OR ");
      }
      builder.append(COLUMN_ID);
      builder.append(" = ");
      builder.append(id);
      i++;
    }
    Cursor cursor = DownloadProvider.getInstance(context).query(
        DownloadConstants.Database.CONTENT_URI,
        null, builder.toString(), null, null);
    try {
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        infos.add(readFromDatabase(cursor));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return infos;
  }

  /**
   * Get all download infos To be completed.
   * 
   * @param context
   * @return not completed downloads
   */
  public static ConcurrentHashMap<Long, InnerDownloadInfo> getNotCompletedDownloads(
      Context context) {
    ConcurrentHashMap<Long, InnerDownloadInfo> downloads =
        new ConcurrentHashMap<Long, InnerDownloadInfo>();
    Cursor cursor = null;

    try {
      cursor = DownloadProvider.getInstance(context).query(DownloadConstants.Database.CONTENT_URI,
          null, COLUMN_STATUS + "!=" + DownloadConstants.Status.STATUS_SUCCESS, null, null);
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        InnerDownloadInfo info = readFromDatabase(cursor);
        // regulateCurrentBytes(info);
        downloads.put(info.mId, info);
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }

    return downloads;
  }

  /**
   * Traverse the whole database to find all identity and id map.
   * 
   * @param context
   * @return IdentityIdMap
   */
  public static HashMap<String, Long> getDownloadIdentityMap(Context context) {
    HashMap<String, Long> maps = new HashMap<String, Long>();
    Cursor cursor = null;
    try {
      cursor =
          DownloadProvider.getInstance(context).query(
              DownloadConstants.Database.CONTENT_URI,
              new String[] {
                  DownloadConstants.Database.COLUMNS.COLUMN_ID,
                  DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY
              },
              null, null, DownloadConstants.Database.COLUMNS.COLUMN_ID + " ASC");
      cursor.moveToFirst();
      int idIndex = cursor.getColumnIndex(DownloadConstants.Database.COLUMNS.COLUMN_ID);
      int identityIndex =
          cursor.getColumnIndex(DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY);
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        maps.put(cursor.getString(identityIndex),
            cursor.getLong(idIndex));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return maps;
  }

  private static void putIfNonNull(ContentValues contentValues, String key,
      Object value) {
    if (value != null) {
      contentValues.put(key, value.toString());
    }
  }

  private static InnerDownloadInfo readFromDatabase(Cursor cursor) {
    InnerDownloadInfo info = new InnerDownloadInfo();
    info.mAllowInMobile = getInt(COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI, cursor) != 0;
    info.mCurrentBytes = getLong(COLUMN_CURRENT_BYTES, cursor);
    info.mDescription = getString(COLUMN_DESCRIPTION, cursor);
    info.mFilePath = getString(COLUMN_FILE_PATH, cursor);
    info.mFolderPath = getString(COLUMN_FOLDER_PATH, cursor);
    // info.mStorePath = getDserviceString(COLUMN_DATA, cursor);
    info.mId = getLong(COLUMN_ID, cursor);
    info.mLastMod = getLong(COLUMN_LAST_MODIFICATION, cursor);
    info.mMimeType = getString(COLUMN_MIME_TYPE, cursor);
    info.mUri = getString(COLUMN_URI, cursor);
    info.mNotificationClass =
        getString(COLUMN_NOTIFICATION_CLASS, cursor);
    info.mType = ResourceType.values()[getInt(COLUMN_RESOURCE_TYPE, cursor)];
    info.mIdentity = getString(COLUMN_IDENTITY, cursor);
    info.mExtras = getString(COLUMN_RESOURCE_EXTRAS, cursor);
    info.setStatus(getInt(COLUMN_STATUS, cursor));
    info.mTitle = getString(COLUMN_TITLE, cursor);
    info.mETag = getString(COLUMN_ETAG, cursor);
    info.mTotalBytes = getLong(COLUMN_TOTAL_BYTES, cursor);
    info.mUserAgent = getString(COLUMN_USE_AGENT, cursor);
    info.mVisible = (getInt(COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI, cursor) == 1);
    info.mNoIntegrity = getInt(COLUMN_NO_INTEGRITY, cursor) == 1;
    info.mNumFailed = getInt(COLUMN_FAILED_TIMES, cursor);
    info.mSource = getString(COLUMN_SOURCE, cursor);
    info.mETag = getString(COLUMN_ETAG, cursor);
    info.mCheckSize = getInt(COLUMN_CHECKSIZE, cursor);
    info.mIconUrl = getString(COLUMN_ICON_URL, cursor);
    info.mDuration = getLong(COLUMN_DURATION, cursor);
    String configuration = getString(COLUMN_SEGMENT_CONFIG, cursor);
    if (!TextUtils.isEmpty(configuration)) {
      info.mConfig = DownloadConfigInfo.fromJson(configuration);
    }
    // assign verify type and value
    String verifyType = getString(COLUMN_VERIFY_TYPE, cursor);
    if (!TextUtils.isEmpty(verifyType)) {
      info.mVerifyType = Enum.valueOf(DownloadConstants.VerifyType.class, verifyType);
    }
    info.mVerifyValue = getString(COLUMN_VERIFY_VALUE, cursor);
    info.mMd5 = getString(COLUMN_MD5_CHECKSUM, cursor);
    info.mRetriedUrls = getString(COLUMN_RETRIED_URLS, cursor);
    info.mLastUrlRetriedTimes = getInt(COLUMN_LAST_URL_RETRIED_TIMES, cursor);
    info.speedLimit = getLong(COLUMN_SPEED_LIMIT, cursor);
    info.setSpeed(getLong(COLUMN_SPEED, cursor));
    String md5StateJson = getString(COLUMN_MD5_STATE, cursor);
    try {
      // lower version's MD5State is different from this version,
      // so the statement maybe throws exception.
      // catch exception to solve the problem.
      md5StateJson = convertOldJsonIfNeed(md5StateJson);
      info.md5State = new Gson().fromJson(md5StateJson, MD5State.class);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return info;
  }

  private static String getString(String column, Cursor cursor) {
    return cursor.getString(cursor.getColumnIndexOrThrow(column));
  }

  private static int getInt(String column, Cursor cusor) {
    return cusor.getInt(cusor.getColumnIndexOrThrow(column));
  }

  private static long getLong(String column, Cursor cursor) {
    return cursor.getLong(cursor.getColumnIndexOrThrow(column));
  }

  public static InnerDownloadInfo getDownloadInfoByIdentity(Context context, String identity) {
    List<InnerDownloadInfo> result = getDownloadInfoByIdentities(context, Arrays.asList(identity));
    if (!result.isEmpty()) {
      return result.get(0);
    }
    return null;
  }

  public static List<InnerDownloadInfo> getDownloadInfoByIdentities(
      Context context, List<String> identities) {
    Cursor cursor = null;
    StringBuilder selectionBuilder = new StringBuilder(
        DownloadConstants.Database.COLUMNS.COLUMN_IDENTITY + " IN (");
    Iterator<String> iter = identities.iterator();
    while (iter.hasNext()) {
      iter.next();
      selectionBuilder.append("?");
      if (iter.hasNext()) {
        selectionBuilder.append(",");
      }
    }
    selectionBuilder.append(")");
    List<InnerDownloadInfo> result = new LinkedList<InnerDownloadInfo>();
    try {
      cursor =
          DownloadProvider.getInstance(context).query(
              DownloadConstants.Database.CONTENT_URI,
              null, selectionBuilder.toString(), identities.toArray(new String[0]),
              DownloadConstants.Database.COLUMNS.COLUMN_ID + " DESC");
      for (cursor.moveToFirst(); !cursor.isAfterLast(); cursor.moveToNext()) {
        result.add(readFromDatabase(cursor));
      }
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      if (cursor != null) {
        cursor.close();
      }
    }
    return result;
  }

  private static String convertOldJsonIfNeed(String json) {
    // return if null
    if (TextUtils.isEmpty(json)) {
      return json;
    }
    try {
      JSONObject obj = new JSONObject(json);
      if (obj != null && obj.has("buffer") && obj.has("count") && obj.has("state")) {
        // for new version, just return json
        return json;
      } else {
        // for old version, try covert the json object
        return covertOldJsonObject(obj).toString();
      }
    } catch (Exception e) {
      return json;
    }
  }

  public static JSONObject covertOldJsonObject(JSONObject oldObj) {
    try {
      JSONObject newObj = new JSONObject();
      Iterator<String> it = oldObj.keys();
      while (it.hasNext()) {
        String key = it.next();
        try {
          // for attr count
          newObj.put("count", oldObj.getLong(key));
          continue;
        } catch (Exception e) {
          // ignore
        }
        try {
          JSONArray arr = oldObj.getJSONArray(key);
          if (arr.length() == 64) {
            // for attr buffer
            newObj.put("buffer", arr);
          } else if (arr.length() == 4) {
            // for attr state
            newObj.put("state", arr);
          }
          continue;
        } catch (Exception e) {
          e.printStackTrace();
        }
      }
      return newObj;
    } catch (Exception e) {
      return oldObj;
    }
  }
}
