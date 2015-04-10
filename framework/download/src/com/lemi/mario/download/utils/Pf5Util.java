package com.lemi.mario.download.utils;

import android.content.Context;

import com.twmacinta.util.MD5;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Used in Data packet verify.
 * Pick the first, the middle, and the last CHUNK_SIZE part of file to
 * calc md5.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class Pf5Util {

  // NOTE: this value can not be changed !!!
  // Chunk size means the size of verify unit block size.
  private static final int CHUNK_SIZE = 1024 * 512;
  private static final int CHUNK_LIMIT = 8;

  private static final int BUFFER_SIZE = 1024;

  /**
   * calc file pf5 value
   * 
   * @param filePath file storage path
   * @return pf5 value, or null if exception happens
   * @throws java.io.IOException
   */
  public static String getPf5String(Context context, String filePath) throws IOException {

    File file = new File(filePath);
    if (file.length() <= CHUNK_SIZE * CHUNK_LIMIT) {
      InputStream input = null;
      try {
        input = new FileInputStream(file);
        return MD5Util.getMd5(context, input);
      } finally {
        close(input);
      }
    }

    long size = file.length();
    byte[] buffer = new byte[BUFFER_SIZE];

    ByteArrayOutputStream byteStream = new ByteArrayOutputStream();
    DataOutputStream dataOutputStream = new DataOutputStream(byteStream);

    RandomAccessFile testFile = new RandomAccessFile(file, "r");

    MD5 md5 = new MD5(context);
    try {
      // First chunk.
      int startPos = CHUNK_SIZE;
      readAndUpdateMD5File(testFile, md5, startPos, CHUNK_SIZE, buffer);

      // Mid chunk.
      long midPos = (size / 2) & ~(CHUNK_SIZE - 1);
      readAndUpdateMD5File(testFile, md5, midPos, CHUNK_SIZE, buffer);

      // End chunk.
      long endPos = (size - CHUNK_SIZE * 2) & ~(CHUNK_SIZE - 1);
      readAndUpdateMD5File(testFile, md5, endPos, CHUNK_SIZE, buffer);

      // consider size.
      ByteBuffer byteBuffer = ByteBuffer.allocate(8);
      byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
      byteBuffer.putLong(size);
      md5.Update(byteBuffer.array());
      return MD5Util.getHexString(md5);
    } catch (IOException e) {
      e.printStackTrace();
    } finally {
      close(testFile);
      close(dataOutputStream);
    }
    return null;
  }

  private static void readAndUpdateMD5File(RandomAccessFile file, MD5 md5,
      long startPos, int size,
      byte[] buffer)
      throws IOException {
    int bytesRead = 0;
    file.seek(startPos);
    while (bytesRead < size) {
      int length = Math.min(BUFFER_SIZE, size - bytesRead);
      int readSize = file.read(buffer, 0, length);
      bytesRead += readSize;
      md5.Update(buffer, 0, readSize);
    }
  }

  private static void close(Closeable dest) {
    if (dest != null) {
      try {
        dest.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }
}
