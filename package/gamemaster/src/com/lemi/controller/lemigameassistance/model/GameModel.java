package com.lemi.controller.lemigameassistance.model;

import java.io.Serializable;
import java.util.List;



/**
 * @author liuxu5@letv.com (Liu Xu)
 */
public class GameModel implements Serializable {

  private long gid;
  private String name;
  private String apkUrl;
  /**
   * php time accurate to second , trans to java time need * 1000
   */
  private long onlineTime;
  /**
   * accurate to MB
   */
  private String apkSize;
  private String realSize;
  private long downloadCount;
  private List<CategoryModel> category;
  private int playMode;
  private int operationMode;
  private int personMode;
  private String author;
  private String versionName;
  private String star;
  private String language;
  private String packageName;
  private String notice;
  private String description;
  private String iconUrl;
  private List<ImageInnerItem> images;
  private List<ImageInnerItem> posters;


  public static final class ImageInnerItem implements Serializable {
    private String url;

    public String getUrl() {
      return url;
    }
  }

  public GameModel() {
  }

  public GameModel(long gid, String name, String apkUrl, long onlineTime, String apkSize,
      String realSize, long downloadCount, List<CategoryModel> category, int playMode,
      int operationMode, int personMode, String author, String versionName, String star,
      String language, String packageName, String notice, String description, String iconUrl,
      List<ImageInnerItem> images, List<ImageInnerItem> posters) {
    this.gid = gid;
    this.name = name;
    this.apkUrl = apkUrl;
    this.onlineTime = onlineTime;
    this.apkSize = apkSize;
    this.realSize = realSize;
    this.downloadCount = downloadCount;
    this.category = category;
    this.playMode = playMode;
    this.operationMode = operationMode;
    this.personMode = personMode;
    this.author = author;
    this.versionName = versionName;
    this.star = star;
    this.language = language;
    this.packageName = packageName;
    this.notice = notice;
    this.description = description;
    this.iconUrl = iconUrl;
    this.images = images;
    this.posters = posters;
  }

  public long getGid() {
    return gid;
  }

  public String getName() {
    return name;
  }

  public String getApkUrl() {
    return apkUrl;
  }

  public long getOnlineTime() {
    return onlineTime;
  }

  public String getApkSize() {
    return apkSize;
  }

  public String getRealSize() {
    return realSize;
  }

  public long getDownloadCount() {
    return downloadCount;
  }

  public List<CategoryModel> getCategory() {
    return category;
  }

  public int getPlayMode() {
    return playMode;
  }

  public int getOperationMode() {
    return operationMode;
  }

  public int getPersonMode() {
    return personMode;
  }

  public String getAuthor() {
    return author;
  }

  public String getVersionName() {
    return versionName;
  }

  public String getStar() {
    return star;
  }

  public String getLanguage() {
    return language;
  }

  public String getPackageName() {
    return packageName;
  }

  public String getNotice() {
    return notice;
  }

  public String getDescription() {
    return description;
  }

  public String getIconUrl() {
    return iconUrl;
  }

  public List<ImageInnerItem> getImages() {
    return images;
  }

  public List<ImageInnerItem> getPosters() {
    return posters;
  }


  public void setGid(long gid) {
    this.gid = gid;
  }

  public void setName(String name) {
    this.name = name;
  }

  public void setApkUrl(String apkUrl) {
    this.apkUrl = apkUrl;
  }

  public void setOnlineTime(long onlineTime) {
    this.onlineTime = onlineTime;
  }

  public void setApkSize(String apkSize) {
    this.apkSize = apkSize;
  }

  public void setRealSize(String realSize) {
    this.realSize = realSize;
  }

  public void setDownloadCount(long downloadCount) {
    this.downloadCount = downloadCount;
  }

  public void setCategory(List<CategoryModel> category) {
    this.category = category;
  }

  public void setPlayMode(int playMode) {
    this.playMode = playMode;
  }

  public void setOperationMode(int operationMode) {
    this.operationMode = operationMode;
  }

  public void setPersonMode(int personMode) {
    this.personMode = personMode;
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public void setVersionName(String versionName) {
    this.versionName = versionName;
  }

  public void setStar(String star) {
    this.star = star;
  }

  public void setLanguage(String language) {
    this.language = language;
  }

  public void setPackageName(String packageName) {
    this.packageName = packageName;
  }

  public void setNotice(String notice) {
    this.notice = notice;
  }

  public void setDescription(String description) {
    this.description = description;
  }

  public void setIconUrl(String iconUrl) {
    this.iconUrl = iconUrl;
  }

  public void setImages(List<ImageInnerItem> images) {
    this.images = images;
  }

  public void setPosters(List<ImageInnerItem> posters) {
    this.posters = posters;
  }
}
