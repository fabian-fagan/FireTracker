package io.fabianfagan.firetracker.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;

@Service
public class FireDataService {
    
    private static String FIRE_DATA_URL = "https://firms.modaps.eosdis.nasa.gov/data/active_fire/modis-c6.1/csv/MODIS_C6_1_Australia_NewZealand_24h.csv";

    /*
     Uses HTTP request/response to fetch data on active fires from US Gov website:
     https://firms.modaps.eosdis.nasa.gov/active_fire/#firms-txt 

    */
    @PostConstruct
    public void fetchFireData() throws IOException, InterruptedException {
           //create new client  
           HttpClient client = HttpClient.newHttpClient();

           //create request
           HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FIRE_DATA_URL)).build(); 

           //get response and print body
           HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString()); 
           System.out.println(httpResponse.body()); 
    }
}
