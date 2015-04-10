package com.lemi.mario.download.model;

import java.io.Serializable;
import java.util.List;

public class DownloadSegMetaData implements Serializable {

  /**
   * serial Version UID
   */
  private static final long serialVersionUID = -2907061966177689642L;

  private String res_sign;
  private long total_size;
  private long seg_size;
  private String total_md5;
  private String total_crc;
  private List<SegmentInfo> segments;

  public String getRes_sign() {
    return res_sign;
  }

  public long getTotal_size() {
    return total_size;
  }

  public long getSeg_size() {
    return seg_size;
  }

  public String getTotal_md5() {
    return total_md5;
  }

  public String getTotal_crc() {
    return total_crc;
  }

  public List<SegmentInfo> getSegments() {
    return segments;
  }

}
