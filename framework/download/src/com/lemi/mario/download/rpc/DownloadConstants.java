package com.lemi.mario.download.rpc;

import android.net.Uri;

import com.lemi.mario.base.config.GlobalConfig;
import com.lemi.mario.download.utils.HttpUtils;

public class DownloadConstants {

  // Log tag
  public static final String TAG = "DOWNLOAD LIBRARY";

  public static final long MIN_FREE_MEMORY_TO_DOWNLOAD_MULTI_THREAD = 60L; // M

  // Authority
  public static final String AUTHORITY = "com.lemi.mario.download";

  // table name
  public static final String DOWNLOAD_TABLE = "downloads";

  public static final String PRE_PARCELHELPER_STRING = "download";

  public static final int DEFAULT_DOWNLOAD_TOTALBYTES = -1;

  public static final int MIN_THREADS_USED_FOR_ONE_TASK = 1;
  public static final int MAX_THREADS_USED_FOR_ONE_TASK = 3;

  // need log
  public static final boolean DEBUG = true;

  // Download Store root path
  public static final String ROOT_DIR = GlobalConfig.getAppRootDir();

  private DownloadConstants() {}

  public static enum VerifyType {
    MD5, /* md5 checksum of downloadtask */
    PF5 /* calc the begin, the middle and the last content of file */
  }

  public static enum PauseReason {
    PAUSE_BY_APP(Status.STATUS_PAUSED_BY_APP),
    PAUSE_BY_WIFI(Status.STATUS_QUEUED_FOR_WIFI_OR_USB),
    PAUSE_BY_MEDIA(Status.STATUS_QUEUED_FOR_SDCARD_MOUNTED);

    private final int status;

    private PauseReason(int status) {
      this.status = status;
    }

    public int getStatus() {
      return this.status;
    }
  }

  public static enum ResourceType {
    APP, MUSIC, VIDEO, IMAGE, EBOOK, COMIC, PATCH, MISC, UNKNOWN, DATA_PACKET, PLUGIN, ZIP, UPGRADE
  }

  public enum DownloadNetworkType {
    NO_NETWORK, MOBILE, WIFI, USB
  }

  public static class Status {
    /**
     * This download task has been created.
     * Insert into database but has not been started yet
     */
    public static final int STATUS_CREATED = 189;

    /**
     * This download hasn't stated yet.
     */
    public static final int STATUS_PENDING = 191;

    /**
     * This download has started.
     */
    public static final int STATUS_RUNNING = 192;

    /**
     * This download has been paused by the owning app.
     */
    public static final int STATUS_PAUSED_BY_APP = 193;

    /**
     * This download exceeded a size limit for mobile networks and is
     * waiting for a Wi-Fi connection to proceed.
     */
    public static final int STATUS_QUEUED_FOR_WIFI_OR_USB = 196;

    /**
     * if the storage is not prepared.
     */
    public static final int STATUS_QUEUED_FOR_SDCARD_MOUNTED = 197;

    /**
     * This download has successfully completed. Warning: there might be
     * other status values that indicate success in the future. Use
     * isSucccess() to capture the entire category.
     */
    public static final int STATUS_SUCCESS = 200;

    /**
     * This download was canceled and all download operation has been canceled.
     */
    public static final int STATUS_CANCELED = 299;

    /**
     * Delete download task and remove from database.
     */
    public static final int STATUS_DELETED = 300;

    /************************************************************/
    /** Blow error is defined by us, start from 425, which is ***/
    /** the max http error status code. *************************/
    /************************************************************/
    /**
     * This download has completed with an error. Warning: there will be
     * other status values that indicate errors in the future. Use
     * isStatusError() to capture the entire category.
     */
    public static final int STATUS_UNKNOWN_ERROR = 491;

    // /////////////////////////////////////////////////////////////
    // Verification errors.
    // /////////////////////////////////////////////////////////////
    /**
     * General verify error. Use more specific status when appropriate.
     */
    public static final int STATUS_OTHER_VERIFY_ERROR = 485;

    /**
     * File length mis-match with that in request.
     */
    public static final int STATUS_FILE_LENGTH_VERIFY_ERROR = 486;

    /**
     * Fail to verify by CRC.
     */
    public static final int STATUS_CRC_VERIFY_ERROR = 474;

    // /////////////////////////////////////////////////////////////
    // New verify error starts from 1000.
    // /////////////////////////////////////////////////////////////
    /**
     * Fail to verify by MD5.
     */
    public static final int STATUS_MD5_VERIFY_ERROR = 1000;

    /**
     * Fail to verify by PF5.
     */
    public static final int STATUS_PF5_VERIFY_ERROR = 1001;

    // /////////////////////////////////////////////////////////////
    // File errors.
    // /////////////////////////////////////////////////////////////
    /**
     * This download couldn't be completed because of a storage issue.
     * Typically, that's because the filesystem is missing or full. Use the
     * more specific {@link #STATUS_INSUFFICIENT_SPACE_ERROR} and
     * {@link #STATUS_DEVICE_NOT_FOUND_ERROR} when appropriate.
     */
    public static final int STATUS_FILE_ERROR = 492;

    /**
     * File has been deleted.
     */
    public static final int STATUS_FILE_HAS_DELETED = 476;

    /**
     * This download couldn't be completed due to insufficient storage
     * space. Typically, this is because the SD card is full.
     */
    public static final int STATUS_NO_WRITE_PERMISSION = 496;

    /**
     * This download couldn't be completed due to insufficient storage
     * space. Typically, this is because the SD card is full.
     */
    public static final int STATUS_INSUFFICIENT_SPACE_ERROR = 498;

    /**
     * This download couldn't be completed because no external storage
     * device was found. Typically, this is because the SD card is not
     * mounted.
     */
    public static final int STATUS_DEVICE_NOT_FOUND_ERROR = 499;
    // New file error starts from 2000.


    // /////////////////////////////////////////////////////////////
    // Http/info errors.
    // /////////////////////////////////////////////////////////////
    /**
     * Failed to get real url from clip-info (which is used to download parallelly).
     */
    public static final int STATUS_CLIP_INFO_ERROR = 473;

    /**
     * The content length from response header is not the same with given length.
     */
    public static final int STATUS_CONTENT_LENGTH_MISMATCH = 477;

    /**
     * Can't resolve redirect url.
     */
    public static final int STATUS_RESOLVE_REDIRECT_URL_FAILED = 479;

    /**
     * Can't know the size to download.
     */
    public static final int STATUS_ACTUALLY_SIZE_UNKNOWN = 480;

    /**
     * Download has retried too much times, and finally give up.
     */
    public static final int STATUS_REACHED_MAX_RETRIED_TIMES = 481;

    /**
     * Downloaded size exceeds request length.
     */
    public static final int STATUS_DOWNLOADED_BYTES_OVERFLOW = 482;

    /**
     * This download couldn't be completed because there were too many
     * redirects.
     */
    public static final int STATUS_TOO_MANY_REDIRECTS = 497;

    /**
     * This download couldn't be completed due to server error.
     */
    public static final int STATUS_SERVER_ERROR = 501;

    // /////////////////////////////////////////////////////////////
    // New http/info error starts from 3000.
    // /////////////////////////////////////////////////////////////
    /**
     * failed to connect server with 60s.
     */
    public static final int STATUS_CONNECTION_TIMEOUT = 3001;

    /**
     * This download couldn't be started due to no dlink URIs are fetched.
     */
    public static final int STATUS_NO_DLINK_URIS = 3002;
  }

  public static class Database {
    // content uri
    public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY
        + "/" + DOWNLOAD_TABLE);

    public static class COLUMNS {
      // need to be "_id"
      public static final String COLUMN_ID = "_id";
      // eg. image/jpeg
      public static final String COLUMN_MIME_TYPE = "mimetype";
      public static final String COLUMN_STATUS = "status";
      public static final String COLUMN_LAST_MODIFICATION = "lastmod";
      public static final String COLUMN_IS_VISIBLE_IN_DOWNLOADS_UI = "visible";
      // Just file folder path, such as /sdcard/leimi/apps/, reuse old column name
      public static final String COLUMN_FOLDER_PATH = "filename";
      public static final String COLUMN_TOTAL_BYTES = "total_bytes";
      public static final String COLUMN_CURRENT_BYTES = "current_bytes";
      public static final String COLUMN_TITLE = "title";
      public static final String COLUMN_DESCRIPTION = "description";
      public static final String COLUMN_USE_AGENT = "use_agent";
      public static final String COLUMN_URI = "uri";
      public static final String COLUMN_DESTINATION = "destination";
      public static final String COLUMN_NOTIFICATION_CLASS = "notification_class";
      public static final String COLUMN_NOTIFICATION_EXTRAS = "notification_extras";
      public static final String COLUMN_ALLOWED_DOWNLOAD_WITHOUT_WIFI =
          "allowed_download_without_wifi";

      // Full file and its path, such as /sdcard/lemi/apps/qq.apk
      public static final String COLUMN_FILE_PATH = "_data"; // where the data

      public static final String COLUMN_ETAG = "etag";
      public static final String COLUMN_NO_INTEGRITY = "no_integrity";
      public static final String COLUMN_SOURCE = "source";
      // ADD MORE
      public static final String COLUMN_RESOURCE_TYPE = "resource_type";
      public static final String COLUMN_RESOURCE_EXTRAS = "resource_extras";
      public static final String COLUMN_IDENTITY = "resouce_identity";
      public static final String COLUMN_CHECKSIZE = "check_size";
      public static final String COLUMN_ICON_URL = "icon_url";
      // Time download cost
      public static final String COLUMN_DURATION = "duration";
      public static final String COLUMN_RETRIED_URLS = "retried_urls";
      public static final String COLUMN_LAST_URL_RETRIED_TIMES = "last_url_retried_times";
      // failed times
      public static final String COLUMN_FAILED_TIMES = "failed_times";
      // download segment config
      public static final String COLUMN_SEGMENT_CONFIG = "segment_config";
      // md5 checksum
      public static final String COLUMN_MD5_CHECKSUM = "md5_checksum";
      public static final String COLUMN_MD5_STATE = "md5_state";
      public static final String COLUMN_SPEED_LIMIT = "speed_limit";
      public static final String COLUMN_SPEED = "speed";
      // verify type and value
      public static final String COLUMN_VERIFY_VALUE = "verify_value";
      public static final String COLUMN_VERIFY_TYPE = "verify_type";
    }
  }

  public static final String DEFAULT_AGENT = HttpUtils.getDefaultHttpClient();
}
