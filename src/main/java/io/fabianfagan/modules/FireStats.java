package io.fabianfagan.modules;

public class FireStats {
    private String country;
    private String lat;
    private String lon; 
    private String time; 
    private String brightness;


    public String getCountry() {
        return country;
    }

    public String getBrightness() {
        return brightness;
    }

    public void setBrightness(String brightness) {
        this.brightness = brightness;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getLon() {
        return lon;
    }

    public void setLon(String lon) {
        this.lon = lon;
    }
    public String getLat() {
        return lat;
    }

    public void setLat(String lat) {
        this.lat = lat;
    }

    public void setCountry(String country) {
        this.country = country;
    } 

    @Override
    public String toString() {
        return "{" +
            " country='" + getCountry() + "'" +
            ", lat='" + getLat() + "'" +
            ", lon='" + getLon() + "'" +
            ", time='" + getTime() + "'" +
            ", brightness='" + getBrightness() + "'" +
            "}";
    }
    
}
