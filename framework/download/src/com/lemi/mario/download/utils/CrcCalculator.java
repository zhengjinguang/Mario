package com.lemi.mario.download.utils;

import com.lemi.mario.download.model.SegmentInfo;

import java.util.List;
import java.util.zip.CRC32;
import java.util.zip.Checksum;

/**
 * utils to calc crc.
 *
 * @author liuxu5@letv.com (Liu Xu)
 */
public class CrcCalculator {

  private List<SegmentInfo> segments;

  private Checksum checksum = new CRC32();

  private int nextCheckingSegPos = 0;

  private long currentBytes = 0;

  private long segSize;

  private boolean isFirst = true;

  /**
   * Constructor.
   *
   * @param segmentInfos crc infos.
   * @param startPos where to start check.
   * @param segSize the size of crc seg.
   */
  public CrcCalculator(List<SegmentInfo> segmentInfos, long startPos, long segSize) {
    this.segments = segmentInfos;
    nextCheckingSegPos = (int) Math.ceil((double) startPos / (double) segSize);
    currentBytes = startPos;
    this.segSize = segSize;
  }

  /**
   * Check crc from given byte data, if the result doesn't match will throw
   * crc check failed exception.
   *
   * @param data
   * @param numOfBytes
   * @throws CrcVerifiedException when verify crc failed.
   */
  public void updateAndCheckCrcValues(byte[] data, int numOfBytes) throws CrcVerifiedException {
    currentBytes += numOfBytes;
    if (isFirst && currentBytes <= nextCheckingSegPos * segSize) {
      return;
    }
    if (currentBytes >= nextCheckingSegPos * segSize) {

      if (!isFirst) {
        checksum.update(data, 0, numOfBytes - (int) (currentBytes - nextCheckingSegPos * segSize));

        String crc = Long.toHexString(checksum.getValue());
        if (!crc.equals(segments.get(nextCheckingSegPos - 1).getCrc())) {
          throw new CrcVerifiedException("crc result not equals the given one");
        }
        checksum.reset();
      }

      isFirst = false;
      if (currentBytes != nextCheckingSegPos * segSize) {
        checksum.update(data, numOfBytes - (int) (currentBytes - nextCheckingSegPos * segSize),
            (int) (currentBytes - nextCheckingSegPos * segSize));
      }
      ++nextCheckingSegPos;
    } else {
      checksum.update(data, 0, numOfBytes);
    }
  }

  /**
   * Crc verify failure.
   */
  public static class CrcVerifiedException extends Exception {

    public CrcVerifiedException(String msg) {
      super(msg);
    }

  }

}
