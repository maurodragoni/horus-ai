package eu.fbk.ict.ehealth.virtualcoach.helis.core;

import java.util.ArrayList;

public class LanguagePair {
  public String langCode;
  public String label;
  public ArrayList<String> alternateLabels;

  public LanguagePair(String c, String l) {
    this.langCode = c;
    this.label = l;
    this.alternateLabels = new ArrayList<String>();
  }

  public String getLangCode() {
    return this.langCode;
  }

  public String getLabel() {
    return this.label;
  }

  public ArrayList<String> getAlternateLabels() {
    return alternateLabels;
  }

  public void setAlternateLabels(ArrayList<String> alternateLabels) {
    this.alternateLabels = alternateLabels;
  }
  
  public void addAlternateLabel(String lp) {
    this.alternateLabels.add(lp);
  }
}
