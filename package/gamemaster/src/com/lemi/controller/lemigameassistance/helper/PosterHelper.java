package com.lemi.controller.lemigameassistance.helper;

import android.os.Looper;
import android.text.TextUtils;

import com.lemi.controller.lemigameassistance.config.Constants;
import com.lemi.mario.base.utils.FileUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class PosterHelper {

  private static final String POSTER_DIR = Constants.ROOT_PATH + File.separator + "poster";
  private static final String TMP_SUFFIX = ".tmp";


  public static void syncDownloadPosters(List<String> posterUrls) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw new IllegalStateException("SyncDownload can not be used in main thread.");
    }
    if (posterUrls == null || posterUrls.size() <= 0) {
      return;
    }
    for (String poster : posterUrls) {
      downloadPic(poster);
    }
  }

  public static void syncDownloadPoster(String posterUrl) {
    if (Looper.myLooper() == Looper.getMainLooper()) {
      throw new IllegalStateException("SyncDownload can not be used in main thread.");
    }
    if (TextUtils.isEmpty(posterUrl)) {
      return;
    }
    downloadPic(posterUrl);
  }

  public static String getPosterFileNameFromUrl(String picUrl) {
    if (TextUtils.isEmpty(picUrl)) {
      return null;
    }
    URL url = null;
    try {
      url = new URL(picUrl);
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
    if (url == null) {
      return null;
    }
    String urlFile = url.getFile();
    if (TextUtils.isEmpty(urlFile)) {
      return null;
    }

    return POSTER_DIR + File.separator + urlFile.substring(urlFile.lastIndexOf("/") + 1);
  }



  private static void downloadPic(String picUrl) {
    boolean isSuccess = false;
    String fileName = getPosterFileNameFromUrl(picUrl);
    if (FileUtil.exists(fileName)) {
      return;
    }
    String tmpFileName = getTmpFileName(fileName);
    FileUtil.deleteFile(tmpFileName);
    HttpURLConnection urlConnection = null;
    InputStream inputStream = null;
    URL url = null;
    try {
      url = new URL(picUrl);
      urlConnection = (HttpURLConnection) url.openConnection();
      inputStream = urlConnection.getInputStream();
      mkFolder();
      writeFromInput(tmpFileName, inputStream);
      FileUtil.renameFile(tmpFileName, fileName);
      FileUtil.deleteFile(tmpFileName);
      isSuccess = true;
    } catch (MalformedURLException e) {
      e.printStackTrace();
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      try {
        if (inputStream != null) {
          inputStream.close();
        }
        if (!isSuccess) {
          FileUtil.deleteFile(fileName);
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

  }

  private static void mkFolder() {
    if (!FileUtil.exists(POSTER_DIR)) {
      FileUtil.mkdir(POSTER_DIR);
    }
  }


  private static File writeFromInput(String fileName, InputStream input) {
    if (TextUtils.isEmpty(fileName)) {
      return null;
    }
    File file = null;
    OutputStream output = null;
    try {
      file = new File(fileName);
      output = new FileOutputStream(file);
      byte[] buffer = new byte[1024];

      int length;
      while ((length = (input.read(buffer))) > 0) {
        output.write(buffer, 0, length);
      }

      output.flush();
    } catch (Exception e) {
      e.printStackTrace();
    } finally {
      try {
        if (output != null) {
          output.close();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
    return file;
  }

  private static String getTmpFileName(String fileName) {
    return fileName + TMP_SUFFIX;
  }


}
