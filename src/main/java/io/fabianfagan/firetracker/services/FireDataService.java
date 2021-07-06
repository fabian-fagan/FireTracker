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
 * Service class used to fetch the data of current active fires.
 * Creates a list of FireStats objects.
 * @author Fabian Fagan
 */
@Service
public class FireDataService {

    private static String FIRE_DATA_URL = "https://firms.modaps.eosdis.nasa.gov/data/active_fire/modis-c6.1/csv/MODIS_C6_1_Australia_NewZealand_24h.csv";
    private List<FireStats> allStats = new ArrayList<>(); 
    private int totalAusFires = 0; 
    private int totalNZFires = 0;

    /** 
     * Uses HTTP request/response to fetch data on active fires from US Gov website:
     * https://firms.modaps.eosdis.nasa.gov/active_fire/#firms-txt
     * 
     */
    @PostConstruct
    @Scheduled(cron = "* * 1 * * *") //schedules to update each day 
    public void fetchFireData() throws IOException, InterruptedException {
        List<FireStats> newStats = new ArrayList<>(); 

        // create new client
        HttpClient client = HttpClient.newHttpClient();

        // create request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FIRE_DATA_URL)).build();

        // get response (data)
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        //create a FireStat object for each value and add to list
        for (CSVRecord record : records) {
            FireStats fireStat = new FireStats();     

            //determine country based on longitude  
            if (Double.parseDouble(record.get("longitude")) > 153.637) { //most eastern point of aus!
               fireStat.setCountry("NZ");
               this.totalNZFires++; 
            } 
            else {
                fireStat.setCountry("AUS");
                this.totalAusFires++; 
            }

            //add other fields & add to list
            fireStat.setLat(record.get("latitude"));
            fireStat.setLon(record.get("longitude"));
            fireStat.setTime(record.get("acq_time"));
            fireStat.setBrightness(record.get("brightness"));
            newStats.add(fireStat); 
        }
        this.allStats = newStats; 
    }
    
    /**
     * Getter methods for values used by the HomeController to add to module.
     */

    public List<FireStats> getAllStats() {
        return this.allStats;
    }

    public double getTotalNzFires() {
        return this.totalNZFires;
    }

    public double getTotalAusFires() {
        return this.totalAusFires;
    }

}
