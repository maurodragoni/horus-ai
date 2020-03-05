package eu.fbk.ict.ehealth.virtualcoach.trecenv.clinicaldataschema;

public class CDSMetadata {

  private long start_date;
  private long end_date;
  private long entry_date;
  private String source;
  private String provenance;
  private String user_id;
  private long server_date;
  
  public CDSMetadata() {}

  public long getStart_date() {
    return start_date;
  }

  public void setStart_date(long start_date) {
    this.start_date = start_date;
  }

  public long getEnd_date() {
    return end_date;
  }

  public void setEnd_date(long end_date) {
    this.end_date = end_date;
  }

  public long getEntry_date() {
    return entry_date;
  }

  public void setEntry_date(long entry_date) {
    this.entry_date = entry_date;
  }

  public String getSource() {
    return source;
  }

  public void setSource(String source) {
    this.source = source;
  }

  public String getProvenance() {
    return provenance;
  }

  public void setProvenance(String provenance) {
    this.provenance = provenance;
  }

  public String getUser_id() {
    return user_id;
  }

  public void setUser_id(String user_id) {
    this.user_id = user_id;
  }

  public long getServer_date() {
    return server_date;
  }

  public void setServer_date(long server_date) {
    this.server_date = server_date;
  }
  
}
