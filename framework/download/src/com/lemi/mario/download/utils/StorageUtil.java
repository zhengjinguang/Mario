package com.lemi.mario.download.utils;

import android.os.Environment;
import android.text.TextUtils;

import com.lemi.mario.base.storage.StorageManager;
import com.lemi.mario.base.utils.FileNameUtil;
import com.lemi.mario.base.utils.FileUtil;
import com.lemi.mario.download.rpc.DownloadConstants;
import com.lemi.mario.download.rpc.DownloadConstants.ResourceType;

import java.io.File;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Storage Util used for getting storage info and storage operations.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class StorageUtil {

  private StorageUtil() {}

  /**
   * if the external media is mounted.
   * exceptional:some special phones will return false
   * when there has an internal storage.(haixin T9)
   * 
   * @return if the external media is mounted
   */
  public static boolean isExternalMediaMounted() {
    if (!Environment.getExternalStorageState().equals(
        Environment.MEDIA_MOUNTED)) {
      return false;
    }
    return true;
  }

  /**
   * get file extension by url.
   * 
   * @param url
   * @return file extension
   */
  private static String getExtensionFromUrl(String url) {
    return MimeTypeMap.getFileExtensionFromUrl(url);
  }

  /**
   * get file name by url.
   * 
   * @param url
   * @return file name
   */
  private static String getFileNameFromUrl(String url) {
    return MimeTypeMap.getFileNameFromUrl(url);
  }

  /**
   * function to get default extension by resource type.
   * 
   * @param {@ling}
   * @return the corresponding extension for the type
   */
  public static String getDefaultExtension(ResourceType type) {
    String extension = "";
    switch (type) {
      case APP:
        extension = ".apk";
        break;
      case PATCH:
        extension = ".patch";
        break;
      case IMAGE:
        extension = ".png";
        break;
      case VIDEO:
        extension = ".mp4";
        break;
      case COMIC:
        extension = ".buka";
        break;
      case ZIP:
        extension = ".zip";
        break;
      default:
        break;
    }
    return extension;
  }

  /**
   * function to get available path by resource type.
   * exceptional:it'll return the environment default path when the type is ebook
   * 
   * @param type {@link com.lemi.mario.download.rpc.DownloadConstants.ResourceType}
   * @return the available path if there has
   */
  public static String getExternalContentDirectory(ResourceType type) {
    return getExternalContentDirectory(type, -1);
  }

  /**
   * function to get available path by resource type and size.
   * 
   * @param type {@link com.lemi.mario.download.rpc.DownloadConstants.ResourceType}
   * @param checkSize
   * @return the available path if there has
   */
  public static String getExternalContentDirectory(ResourceType type, long checkSize) {
    return getExternalContentDirectory(StorageManager.getInstance()
        .getExternalStorageDirectory(checkSize), type);
  }

  /**
   * function to get available path by storage directory and resource type.
   * 
   * @param storageDirectory
   * @param type {@link com.lemi.mario.download.rpc.DownloadConstants.ResourceType}
   * @return the available path if there has
   */
  public static String getExternalContentDirectory(String storageDirectory, ResourceType type) {
    if (!TextUtils.isEmpty(storageDirectory)) {
      // make the absolute path (lowercase the enum value)
      String content =
          storageDirectory + "/" + DownloadConstants.ROOT_DIR
              + "/" + type.name().toLowerCase() + "/";

      File contentFile = new File(content);
      if (!contentFile.exists()) {
        if (!contentFile.mkdirs()) {
          return null;
        }
      }
      return content;
    } else {
      return null;
    }
  }

  /**
   * Check whether having enough size to execute downloading task.
   * 
   * @param size
   * @return whether having enough size
   */
  public static boolean hasEnoughSize(long size) {
    // if file size is -1 , do not check
    if (size < 0) {
      return true;
    }
    List<String> availableStoragesPath;
    availableStoragesPath = StorageManager.getInstance().getExternalStorageDirectorys();
    for (String path : availableStoragesPath) {
      if (FileUtil.getAvailableBytes(path) >= size) {
        return true;
      }
    }
    return false;
  }

  /**
   * Creates a filename (where the file should be saved) from info about a download.
   * 
   * @param targetUrl the last redirection url
   * @param targetFolder file folder
   * @param contentDisposition
   * @param type resource type
   * @return generated FilePath.
   * @throws {@link GenerateSaveFileException}
   */
  public static String generateSaveFile(
      String title,
      String targetUrl,
      String targetFolder,
      String contentDisposition,
      ResourceType type,
      long contentLength) throws GenerateSaveFileException {

    if (TextUtils.isEmpty(targetFolder)) {
      if (contentLength > 0) {
        targetFolder = StorageUtil.getExternalContentDirectory(type, contentLength);
      } else {
        targetFolder = StorageUtil.getExternalContentDirectory(type);
      }
    }
    if (!TextUtils.isEmpty(targetFolder)) {
      targetFolder = targetFolder.replaceFirst("file://", "");
      if (!targetFolder.endsWith("/")) {
        targetFolder += "/";
      }
      File targetFolderFile = new File(targetFolder);
      if (!targetFolderFile.exists()) {
        if (!targetFolderFile.mkdirs()) {
          throw new GenerateSaveFileException(
              DownloadConstants.Status.STATUS_FILE_ERROR, "unable to make folder");
        }
      }

      String fileName;
      if (!TextUtils.isEmpty(title)) {
        fileName = title;
      } else {
        fileName = getFileNameFromUrl(targetUrl);
      }
      if (!TextUtils.isEmpty(fileName)) {
        fileName = FileNameUtil.removeIllegalChars((fileName));
      }
      String extension = getExtensionFromUrl(targetUrl);
      if (TextUtils.isEmpty(extension)) {
        extension = getDefaultExtension(type);
      }

      String filePath = null;
      if (!TextUtils.isEmpty(fileName) && !TextUtils.isEmpty(extension)) {
        filePath = targetFolder + fileName + "_" + System.currentTimeMillis() + "." + extension;
      } else {
        if (!TextUtils.isEmpty(contentDisposition)) {
          contentDisposition = contentDisposition.replace("\"", "");
          if (!TextUtils.isEmpty(contentDisposition)) {
            Matcher m =
                Pattern.compile(".*filename=(.*)").matcher(contentDisposition.toLowerCase());
            if (m.find()) {
              String matchResult = m.group(1);
              int index = matchResult.indexOf(';');
              // attachment; filename="abbas.pdf"; modification-date="Tue, 10 Jun 2014 1"
              if (index != -1) {
                matchResult = matchResult.substring(0, index);
              }
              filePath = targetFolder + System.currentTimeMillis() + "_" + matchResult;
            }
          }
        }
      }

      if (TextUtils.isEmpty(filePath)) {
        throw new GenerateSaveFileException(
            DownloadConstants.Status.STATUS_FILE_ERROR,
            "unable to generate file name");
      }

      if (FileUtil
          .getAvailableBytes(getFilesystemRoot(filePath).getAbsolutePath()) < contentLength) {
        throw new GenerateSaveFileException(
            DownloadConstants.Status.STATUS_INSUFFICIENT_SPACE_ERROR,
            "insufficient space on external storage");
      }

      return filePath;
    }
    return null;
  }

  /**
   * @return the root of the filesystem containing the given path
   */
  public static File getFilesystemRoot(String path) {
    File cache = Environment.getDownloadCacheDirectory();
    if (path.startsWith(cache.getPath())) {
      return cache;
    }
    File external = new File(FileNameUtil.getFullPath(path));
    if (external.exists()) {
      return external;
    }
    throw new IllegalArgumentException(
        "Cannot determine filesystem root for " + path);
  }

  /**
   * Exception thrown from methods called by generateSaveFile() for any fatal
   * error.
   */
  public static class GenerateSaveFileException extends Exception {
    private static final long serialVersionUID = -7012378141199515715L;

    int mStatus;
    String mMessage;

    public int getStatus() {
      return mStatus;
    }

    public String getMessage() {
      return mMessage;
    }

    public GenerateSaveFileException(int status, String message) {
      mStatus = status;
      mMessage = message;
    }
  }
}
