package com.lemi.mario.download.rpc;

import android.text.TextUtils;

import com.lemi.mario.download.model.segments.DownloadConfigInfo;
import com.twmacinta.util.MD5State;

/**
 * Store information about an individual download.
 * Different to {@link InnerDownloadRequest}, it's nearly a mirror to the item in download Database,
 * besides it store some data in memory when downloading data in the thread.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class InnerDownloadInfo {

  private static final String DOWNLOADING_FILE_SUFFIX = ".download";
  /* public access */
  public long mId; // default value
  public String mUri;
  public String mMimeType;

  public String mTitle;
  public String mDescription;
  public long mTotalBytes;

  public DownloadConstants.ResourceType mType;
  public String mIdentity;
  public String mExtras;
  public long mDuration;
  public String mSource;
  public String mIconUrl;
  // The whole path of the file, such as /sdcard/lemi/apps/qq.apk
  public String mFilePath;
  public String mRetriedUrls;
  public int mLastUrlRetriedTimes;
  public boolean mVisible;
  public long mLastMod;
  public long mCurrentBytes;
  public String mMd5;
  public long speedLimit;
  public DownloadConstants.VerifyType mVerifyType;
  public String mVerifyValue;

  public DownloadConfigInfo mConfig;
  public long mSpeed;

  // The folder path, such as /sdcard/lemi/apps/
  public String mFolderPath;
  public String mETag;
  public boolean mAllowInMobile;
  public int mNumFailed;
  public int mRetryAfter;
  public boolean mNoIntegrity;
  public String mNotificationClass;
  public String mUserAgent;
  public int mCheckSize;
  public MD5State md5State;

  private int mStatus;
  // previous status, current only used for log
  public int previousStatus;

  public void setSpeed(long speed) {
    if (speed > 0) {
      mSpeed = speed;
    }
  }

  public long getSpeed() {
    if (mStatus == DownloadConstants.Status.STATUS_RUNNING) {
      return mSpeed;
    }
    return 0L;
  }

  public long getLastSpeed() {
    return mSpeed;
  }

  public long getLastMod() {
    return mLastMod;
  }

  public void setStatus(int status) {
    // save refer status
    previousStatus = this.mStatus;
    mStatus = status;
  }

  public int getStatus() {
    return mStatus;
  }

  public String getIntermediateFilePath() {
    if (TextUtils.isEmpty(mFilePath)) {
      return null;
    }
    return mFilePath + DOWNLOADING_FILE_SUFFIX;
  }

  @Override
  public InnerDownloadInfo clone() {
    InnerDownloadInfo dest = new InnerDownloadInfo();

    dest.mCurrentBytes = mCurrentBytes;
    dest.mDescription = mDescription;
    dest.mDuration = mDuration;
    dest.mFolderPath = mFolderPath;
    dest.mIconUrl = mIconUrl;
    dest.mId = mId;
    dest.mIdentity = mIdentity;
    dest.mFilePath = mFilePath;
    dest.mMimeType = mMimeType;
    dest.mExtras = mExtras;
    dest.mSource = mSource;
    dest.mStatus = mStatus;
    dest.previousStatus = previousStatus;
    dest.mTitle = mTitle;
    dest.mTotalBytes = mTotalBytes;
    dest.mType = mType;
    dest.mUri = mUri;
    dest.mRetriedUrls = mRetriedUrls;
    dest.mLastUrlRetriedTimes = mLastUrlRetriedTimes;
    dest.mETag = mETag;
    dest.mAllowInMobile = mAllowInMobile;
    dest.mVisible = mVisible;
    dest.mNumFailed = mNumFailed;
    dest.mRetryAfter = mRetryAfter;
    dest.mLastMod = mLastMod;
    dest.mNoIntegrity = mNoIntegrity;
    dest.mNotificationClass = mNotificationClass;
    dest.mUserAgent = mUserAgent;
    dest.mCheckSize = mCheckSize;
    dest.mConfig = mConfig;
    dest.mSpeed = mSpeed;
    dest.mMd5 = mMd5;
    dest.speedLimit = speedLimit;
    dest.md5State = md5State;

    return dest;
  }
}
