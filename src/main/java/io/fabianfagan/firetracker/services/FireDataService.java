package io.fabianfagan.firetracker.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.fabianfagan.firetracker.modules.FireStats;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

/**
 * Service class used to fetch the data of current active fires. Creates a list
 * of FireStats objects.
 * 
 * @author Fabian Fagan
 */
@Service
public class FireDataService {
    private static String FIRE_DATA_URL = "https://firms.modaps.eosdis.nasa.gov/data/active_fire/modis-c6.1/csv/MODIS_C6_1_Australia_NewZealand_24h.csv"; 
    //Fields
    private List<FireStats> allFireStats = new ArrayList<>();
    private List<FireStats> ausStats = new ArrayList<>();
    private List<FireStats> nzStats = new ArrayList<>();
    private List<FireStats> piStats = new ArrayList<>();
    private int[] areaTotals = new int[13];
    private int totalAusFires = 0;
    private int totalNZFires = 0;
    private int totalPIFires = 0;
    

    /**
     * Uses HTTP request/response to fetch data on active fires from US Gov website:
     * https://firms.modaps.eosdis.nasa.gov/active_fire/#firms-txt
     * 
     */
    @PostConstruct
    @Scheduled(cron = "* * 1 * * *") // schedules to update each day
    public void fetchFireData() throws IOException, InterruptedException {
        // Create new lists for concurrency issues
        List<FireStats> newAllStats = new ArrayList<>();
        List<FireStats> newAusStats = new ArrayList<>();
        List<FireStats> newNzStats = new ArrayList<>();
        List<FireStats> newPiStats = new ArrayList<>();

        // create new client
        HttpClient client = HttpClient.newHttpClient();

        // create request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FIRE_DATA_URL)).build();

        // get response (fire data)
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        // create a FireStat object for each value and add to list
        String date = java.time.LocalDate.now().toString(); 
        int currentFireID = 0; 
        for (CSVRecord record : records) {
            FireStats fireStat = new FireStats();

            // determine country based on longitude and latitude
            if (Double.parseDouble(record.get("longitude")) > 153.637) { // more east than Australia
                if (Double.parseDouble(record.get("latitude")) > -34.394) { // more north than NZ
                    fireStat.setCountry("PI");
                    newPiStats.add(fireStat);
                    this.totalPIFires++;
                } else {// NZ
                    fireStat.setCountry("NZ");
                    newNzStats.add(fireStat);
                    this.totalNZFires++;
                }
            } else {// Australia
                fireStat.setCountry("AUS");
                newAusStats.add(fireStat);
                this.totalAusFires++;
            }

            // add other fields & add to list
            String lat = record.get("latitude");
            String lon = record.get("longitude");
            fireStat.setArea(calculateArea(lat, lon, fireStat.getCountry()));
            fireStat.setLat(lat);
            fireStat.setLon(lon);
            fireStat.setTime(record.get("acq_time"));
            fireStat.setBrightness(record.get("brightness"));
            fireStat.setId(date + ":" + currentFireID++);
            newAllStats.add(fireStat);
        }
        this.allFireStats = newAllStats;
        this.ausStats = newAusStats;
        this.nzStats = newNzStats;
        this.piStats = newPiStats;
    }

    /**
     * Getter methods for values used by the HomeController to add to module.
     */

    public List<FireStats> getAllStats() {
        return this.allFireStats;
    }

    public List<FireStats> getAusStats() {
        return this.ausStats;
    }

    public List<FireStats> getNzStats() {
        return this.nzStats;
    }

    public List<FireStats> getPiStats() {
        return this.piStats;
    }

    public int getTotalNzFires() {
        return this.totalNZFires;
    }

    public int getTotalAusFires() {
        return this.totalAusFires;
    }

    public int getTotalPIFires() {
        return this.totalPIFires;
    }

    /**
     * Calculates the estimated area (state for AUS, north/south for NZ & island for
     * PIs). Also tallys area totals. Messy, but cheaper than calling a Geocoding
     * API for every entry!
     * 
     * @param lat
     * @param lon
     * @return Estimated area
     */
    private String calculateArea(String latitude, String longitude, String country) {
        double lat = Double.parseDouble(latitude);
        double lon = Double.parseDouble(longitude);
        if (country.equals("NZ")) {
            if (lat > -41.33) {
                this.areaTotals[0]++;
                return "North Island";
            } else {
                this.areaTotals[1]++;
                return "South Island";
            }
        } else if (country.equals("PI")) {
            if (lon < 177) {
                this.areaTotals[2]++;
                return "New Calidonia/Vanuatu";
            }
            if (lon > 177.00 && lon < 179.99) {
                this.areaTotals[3]++;
                return "Fiji";
            }
            if (lon > -172.8 && lat > 14.55) {
                this.areaTotals[4]++;
                return "Samoa";
            } else {
                this.areaTotals[5]++;
                return "Tonga/Niue/Cook Islands";
            }
        } else if (country.equals("AUS")) {
            if (lon < 129) {
                this.areaTotals[6]++;
                return "Western Australia";
            }
            if (lon > 129 && lon < 141) {
                if (lat > -26) {
                    this.areaTotals[7]++;
                    return "Northern Territory";
                } else {
                    this.areaTotals[8]++;
                    return "South Australia";
                }
            }
            if (lon > 141) {
                if (lat > -28.55) {
                    this.areaTotals[9]++;
                    return "Queensland";
                } else {
                    this.areaTotals[10]++;
                    return "NSW";
                }
            } else if (lat < -39.00) {
                this.areaTotals[11]++;
                return "Tasmania";
            }
        }
        this.areaTotals[12]++;
        return "Unknown";
    }


    /**
     * Return the total amount of fires in the requested area.
     * @param area to find total
     * @return total in area
     */
    public int getAreaTotal(String area) {
        switch (area) {
            case "North Island":
            return this.areaTotals[0];           
        
            case "South Island":
            return this.areaTotals[1];
               
            case "New Calidonia/Vanuatu":
            return this.areaTotals[2];
                         
            case "Fiji":
            return this.areaTotals[3];            

            case "Samoa":
            return this.areaTotals[4];           

            case "Tonga/Niue/Cook Islands":
            return this.areaTotals[5];
               
            case "Western Australia":
            return this.areaTotals[6];
               
            case "Northern Territory":
            return this.areaTotals[7];              

            case "South Australia":
            return this.areaTotals[8];
               
            case "Queensland":
            return this.areaTotals[9];
               
            case "NSW":
            return this.areaTotals[10];
               
            case "Tasmania":
            return this.areaTotals[11];    
            
            default:
            return this.areaTotals[12]; //Unknown area
        }
    }

}
