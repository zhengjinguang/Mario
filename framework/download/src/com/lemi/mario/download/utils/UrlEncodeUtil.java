package com.lemi.mario.download.utils;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.BitSet;

/**
 * Utils to encode url.
 * 
 * @author liuxu5@letv.com (Liu Xu)
 */
public class UrlEncodeUtil {

  private UrlEncodeUtil() {};

  /**
   * Array containing the safe characters set as defined by RFC 1738
   */
  private static BitSet safeCharacters;

  private static final int BIT_SET_SIZE = 256;

  private static final char[] hexadecimal =
  {'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
      'A', 'B', 'C', 'D', 'E', 'F'};

  static {
    safeCharacters = new BitSet(BIT_SET_SIZE);
    int i;
    // 'lowalpha' rule
    for (i = 'a'; i <= 'z'; i++) {
      safeCharacters.set(i);
    }
    // 'hialpha' rule
    for (i = 'A'; i <= 'Z'; i++) {
      safeCharacters.set(i);
    }
    // 'digit' rule
    for (i = '0'; i <= '9'; i++) {
      safeCharacters.set(i);
    }

    // 'safe' rule
    safeCharacters.set('$');
    safeCharacters.set('-');
    safeCharacters.set('_');
    safeCharacters.set('.');
    safeCharacters.set('+');
    safeCharacters.set('?');
    safeCharacters.set('#');
    safeCharacters.set(';');
    safeCharacters.set('%');

    // 'extra' rule
    safeCharacters.set('!');
    safeCharacters.set('*');
    safeCharacters.set('\'');
    safeCharacters.set('(');
    safeCharacters.set(')');
    safeCharacters.set(',');

    // special characters common to http: file: and ftp: URLs ('fsegment' and 'hsegment' rules)
    safeCharacters.set('/');
    safeCharacters.set(':');
    safeCharacters.set('@');
    safeCharacters.set('&');
    safeCharacters.set('=');
  }


  /**
   * Encode url string to make <code>HttpGet(String url)</code> work right.
   * 
   * <p>
   * HttpGet function won't work when url has illegal char like blank, and in other hand, some other
   * chars like '/', '#', should not be encoded cause if do that, url won't be parsed.
   * </p>
   * 
   * <p>
   * example: m/search/list/detail/ 重装机兵 /20/normal&udid=97c22fdb5de64e808952d8ef42caec9ac1c643e4
   * encoded to :
   * m/search/list/detail/%C2%A0%E9%87%8D%E8%A3%85%E6%9C%BA%E5%85%B5%C2%A0/20/normal&udid
   * =97c22fdb5de64e808952d8ef42caec9ac1c643e4
   * </p>
   * 
   * @param path the path to encode
   * @return the encoded path
   */
  public static String encodeURI(String path) {

    int maxBytesPerChar = 10;
    StringBuilder rewrittenPath = new StringBuilder(path.length());
    ByteArrayOutputStream buf = new ByteArrayOutputStream(maxBytesPerChar);
    OutputStreamWriter writer;
    try {
      writer = new OutputStreamWriter(buf, "UTF8");
    } catch (Exception e) {
      e.printStackTrace();
      writer = new OutputStreamWriter(buf);
    }

    for (int i = 0; i < path.length(); i++) {
      int c = path.charAt(i);
      if (safeCharacters.get(c)) {
        rewrittenPath.append((char) c);
      } else {
        // convert to external encoding before hex conversion
        try {
          writer.write(c);
          writer.flush();
        } catch (IOException e) {
          buf.reset();
          continue;
        }
        byte[] ba = buf.toByteArray();
        for (byte toEncode : ba) {
          // Converting each byte in the buffer
          rewrittenPath.append('%');
          int low = (toEncode & 0x0f);
          int high = ((toEncode & 0xf0) >> 4);
          rewrittenPath.append(hexadecimal[high]);
          rewrittenPath.append(hexadecimal[low]);
        }
        buf.reset();
      }
    }
    return rewrittenPath.toString();
  }

}
