package com.lemi.mario.download.rpc;

import android.content.ContentValues;
import android.content.Context;
import android.util.Log;

import com.lemi.mario.download.rpc.InnerDownloadFilter.FilterArea;

import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The API that operate data in database.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 * 
 */
public class DatabaseDataController {

  private static final String TAG = DatabaseDataController.class.getSimpleName();

  private static DatabaseDataController instance;

  // application context
  private final Context context;

  private DatabaseDataController(final Context context) {
    this.context = context;
  }

  /**
   * Get static Instance.
   * 
   * @return DatabaseDataController instance
   */
  public static synchronized DatabaseDataController getInstance(final Context context) {
    if (instance == null) {
      instance = new DatabaseDataController(context);
    }
    return instance;
  }

  /**
   * Browse the database to find the item which _id column equal id.
   * 
   * @param id
   * @return DownloadInfo if found, or null.
   */
  public InnerDownloadInfo getDownloadInfoById(long id) {
    return DatabaseHelper.getDownloadInfoFromId(context, id);
  }

  public InnerDownloadInfo addDownloadInfo(InnerDownloadRequest request) {
    InnerDownloadInfo info = null;
    long id = DatabaseHelper.insertItem(context, request);
    // if id is negative, it means that can't insert into database
    if (id >= 0) {
      info = DatabaseHelper.getDownloadInfoFromId(context, id);
      if (info != null) {
        Log.d(TAG, "insert into download database success, and the download id is " + info.mId);
      }
    }
    return info;
  }

  public HashMap<String, Long> getIdentityMap() {
    return DatabaseHelper.getDownloadIdentityMap(context);
  }

  /**
   * Query the whole database to find downloads which has not been finished.
   * 
   * <p>
   * NOTE: all unfinished data will be achieved and store in memory
   * </p>
   * 
   * @return unfinished downloadInfos
   */
  public ConcurrentHashMap<Long, InnerDownloadInfo> getAllUnfinishedDownloadInfos() {
    return DatabaseHelper.getNotCompletedDownloads(context);
  }

  public boolean updateDownloadInfo(long id, ContentValues values) {
    int rowNum = DatabaseHelper.updateDownloadInfo(context, id, values);
    return (rowNum == 1);
  }

  public boolean updateDownloadInfo(InnerDownloadInfo info) {
    int row = DatabaseHelper.updateDownloadTaskInfo(context, info);
    return (row == 1);
  }

  public List<InnerDownloadInfo> getDownloadInfosFromIds(List<Long> ids) {
    return DatabaseHelper.getDownloadInfosFromIds(context, ids);
  }

  public boolean removeDownloadTask(long id) {
    int rowNum = DatabaseHelper.deleteItem(context, id);
    return (rowNum == 1);
  }

  public List<InnerDownloadInfo> getDownloadInfos(InnerDownloadFilter filter, int start, int num,
      FilterArea ordercolumn, boolean asc) {
    return DatabaseHelper.getDownloadInfos(context, filter, start, num, ordercolumn, asc);
  }

  public int getDownloadCount(InnerDownloadFilter filter) {
    return DatabaseHelper.getDownloadCount(context, filter);
  }

  public InnerDownloadInfo getDownloadInfoByIdentity(String identity) {
    return DatabaseHelper.getDownloadInfoByIdentity(context, identity);
  }

  public List<InnerDownloadInfo> getDownloadInfoByIdentities(List<String> identities) {
    return DatabaseHelper.getDownloadInfoByIdentities(context, identities);
  }
}
