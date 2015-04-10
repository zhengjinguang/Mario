package com.lemi.mario.accountmanager.model;

import java.io.Serializable;

/**
 * @author zhengjinguang@letv.com (shining).
 */
public class User implements Serializable {

  private String nick;
  private int uid;
  private String udid;
  private String phone;
  private String email;
  private int gender;

  public String getNick() {
    return nick;
  }

  public void setNick(String nick) {
    this.nick = nick;
  }

  public int getUid() {
    return uid;
  }

  public void setUid(int uid) {
    this.uid = uid;
  }

  public String getUdid() {
    return udid;
  }

  public void setUdid(String udid) {
    this.udid = udid;
  }

  public String getEmail() {
    return email;
  }

  public void setEmail(String email) {
    this.email = email;
  }

  public String getPhone() {
    return phone;
  }

  public void setPhone(String phone) {
    this.phone = phone;
  }

  public int getGender() {
    return gender;
  }

  public void setGender(int gender) {
    this.gender = gender;
  }

  @Override
  public boolean equals(Object o) {
    if (o == null) {
      return false;
    }
    if (o instanceof User) {
      User user = (User) o;
      if (user.getUid() == uid
          && user.getNick().equals(nick)
          && user.getUdid().equals(udid)
          && user.getPhone().equals(phone)
          && user.getEmail().equals(email)
          && user.getGender() == gender) {
        return true;
      }
    }
    return false;
  }
}
