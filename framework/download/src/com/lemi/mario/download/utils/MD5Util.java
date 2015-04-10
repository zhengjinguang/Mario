package com.lemi.mario.download.utils;

import android.content.Context;

import com.twmacinta.util.MD5;
import com.twmacinta.util.MD5InputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * MD5Util used to calc md5 values.
 */
public class MD5Util {

  private MD5Util() {}

  private static final String MD5_DIGEST = "MD5";

  /**
   * Calc md5 from given file path.
   * 
   * @param filePath
   * @return md5
   */
  public static String calcMD5FromFile(Context context, String filePath) {
    try {
      return MD5.asHex(MD5.getHash(context, new File(filePath)));
    } catch (IOException e) {
      return null;
    }
  }

  /**
   * Updates the digest using the specified array of bytes.
   *
   * @param md5
   * @param data
   * @param offset
   * @param length
   */
  public static void update(MD5 md5, byte[] data, int offset, int length) {
    md5.Update(data, offset, length);
  }

  /**
   * Completes the hash computation by performing final operations such as padding.
   * 
   * @param md5
   * @return md5 result
   */
  public static String getHexString(MD5 md5) {
    return getHexString(md5.Final());
  }

  public static String getHexString(byte[] digest) {
    return MD5.asHex(digest);
  }

  public static String getMd5(byte[] bytes) {
    try {
      MessageDigest mMessageDigest = MessageDigest.getInstance(MD5_DIGEST);
      mMessageDigest.update(bytes);
      return getHexString(mMessageDigest.digest());
    } catch (NoSuchAlgorithmException e) {
      e.printStackTrace();
    }
    return null;
  }

  public static String getMd5(Context context, InputStream inputStream)
      throws IOException {
    MD5InputStream md5InputStream = new MD5InputStream(context, inputStream);
    return MD5.asHex(md5InputStream.hash());
  }
}
