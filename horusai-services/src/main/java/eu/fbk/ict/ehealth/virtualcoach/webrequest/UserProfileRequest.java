package eu.fbk.ict.ehealth.virtualcoach.webrequest;

import java.util.ArrayList;

import eu.fbk.ict.ehealth.virtualcoach.helis.core.Pair;
import eu.fbk.ict.ehealth.virtualcoach.helis.core.Profile;

public class UserProfileRequest {
  private String mode;
  private String userId;
  private String username;
  private String gender;
  private int height;
  private int weight;
  private long age;
  private ArrayList<Profile> profiles;
  private ArrayList<Pair> properties;

  public UserProfileRequest() {
  }

  public String getUserId() {
    return userId;
  }

  public void setUserId(String userId) {
    this.userId = userId;
  }

  public ArrayList<Profile> getProfiles() {
    return profiles;
  }

  public void setProfiles(ArrayList<Profile> profiles) {
    this.profiles = profiles;
  }
  
  public ArrayList<Pair> getProperties() {
    return properties;
  }

  public void setProperties(ArrayList<Pair> properties) {
    this.properties = properties;
  }

  public String getMode() {
    return mode;
  }

  public void setMode(String mode) {
    this.mode = mode;
  }

  public String getUsername() {
    return username;
  }

  public void setUsername(String username) {
    this.username = username;
  }

  public String getGender() {
    return gender;
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public int getWeight() {
    return weight;
  }

  public void setWeight(int weight) {
    this.weight = weight;
  }

  public long getAge() {
    return age;
  }

  public void setAge(long age) {
    this.age = age;
  }
}
