package com.example.crawlerapi.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

@Document(collection="sights")
@CompoundIndex(name="uniq_name_zone", def="{'sightName':1,'zone':1}", unique=true)
public class SightEntity {
  @Id private String id;
  private String sightName, zone, category, photoURL, address, description;
  public SightEntity() {}
  public SightEntity(String n,String z,String c,String p,String a,String d){
    this.sightName=n; this.zone=z; this.category=c; this.photoURL=p; this.address=a; this.description=d;
  }
  // getters/setters 省略也可用 IDE 產生
  public String getId(){return id;} public void setId(String id){this.id=id;}
  public String getSightName(){return sightName;} public void setSightName(String v){this.sightName=v;}
  public String getZone(){return zone;} public void setZone(String v){this.zone=v;}
  public String getCategory(){return category;} public void setCategory(String v){this.category=v;}
  public String getPhotoURL(){return photoURL;} public void setPhotoURL(String v){this.photoURL=v;}
  public String getAddress(){return address;} public void setAddress(String v){this.address=v;}
  public String getDescription(){return description;} public void setDescription(String v){this.description=v;}
}
