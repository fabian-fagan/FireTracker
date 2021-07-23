package io.fabianfagan.firetracker.modules;

/**
 * An object for holding stats about a singular fire from the dataset. 
 * (country, area, latitude, longitude, time, brightness). 
 * @author Fabian Fagan
 */ 
public class Fire { 
    private String country;
    private String area; 
    private String lat;
    private String lon;  
    private String time; 
    private String brightness;
    private String id; 

     
    /**
     * Getters & Setters
     */

    public String getCountry() {
        return this.country;
    }

    public void setCountry(String country) {
        this.country = country;
    } 

    public String getArea() {
        return this.area;
    }

    public void setArea(String area) {
        this.area = area; 
    }

    public String getBrightness() {
        return this.brightness;
    }

    public void setBrightness(String brightness) {
        this.brightness = brightness;
    }

    public String getTime() {
        return this.time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLon() {
        return this.lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
    public String getLat() {
        return this.lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public String getId() {
        return this.id;
    }

    public void setId(String id) {
        this.id = id;
    }
  
     /**
      * ToString method for testing 
      */
    @Override
    public String toString() {
        return "{" +
            " country='" + getCountry() + "'" +
            ", area='" + getArea() + "'" +
            ", lat='" + getLat() + "'" +
            ", lon='" + getLon() + "'" +
            ", time='" + getTime() + "'" +
            ", brightness='" + getBrightness() + "'" +
            ", ID='" + getId() + "'" +
            "}";
    }
    
}
