package io.fabianfagan.firetracker.services;

import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.PostConstruct;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVRecord;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import io.fabianfagan.firetracker.modules.Fire;

import java.io.IOException;
import java.io.StringReader;
import java.net.URI;

/**
 * Service class used to fetch the data of current active fires. Creates lists
 * of Fire objects, gathers fire totals and calculates fire areas.
 * 
 * @author Fabian Fagan
 */
@Service
public class FireDataService {
    private static String FIRE_DATA_URL = "https://firms.modaps.eosdis.nasa.gov/data/active_fire/modis-c6.1/csv/MODIS_C6_1_Australia_NewZealand_24h.csv";
    // Fields
    private List<Fire> allFires = new ArrayList<>();
    private List<Fire> ausFires = new ArrayList<>();
    private List<Fire> nzFires = new ArrayList<>();
    private List<Fire> piFires = new ArrayList<>();
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
        List<Fire> newAllFires = new ArrayList<>();
        List<Fire> newAusFires = new ArrayList<>();
        List<Fire> newNzFires = new ArrayList<>();
        List<Fire> newPiFires = new ArrayList<>();

        // create new client
        HttpClient client = HttpClient.newHttpClient();

        // create request
        HttpRequest request = HttpRequest.newBuilder().uri(URI.create(FIRE_DATA_URL)).build();

        // get response (fire data)
        HttpResponse<String> httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());
        StringReader csvBodyReader = new StringReader(httpResponse.body());
        Iterable<CSVRecord> records = CSVFormat.DEFAULT.withFirstRecordAsHeader().parse(csvBodyReader);

        // create a Fire object for each non duplicate value and add to lists
        String date = java.time.LocalDate.now().toString();
        int currentFireID = 0;
        for (CSVRecord record : records) {
            Fire fire = new Fire();
            String lat = record.get("latitude");
            String lon = record.get("longitude");
            if (!fireAlreadyExistsAtCoordinate(lat, lon, newAllFires)) {
                // determine country based on longitude and latitude
                if (Double.parseDouble(lon) > 153.637) { // more east than Australia
                    if (Double.parseDouble(lat) > -34.394) { // more north than NZ
                        fire.setCountry("PI");
                        newPiFires.add(fire);
                        this.totalPIFires++;
                    } else {// NZ
                        fire.setCountry("NZ");
                        newNzFires.add(fire);
                        this.totalNZFires++;
                    }
                } else {// Australia
                    fire.setCountry("AUS");
                    newAusFires.add(fire);
                    this.totalAusFires++;
                }

                // add other fields & add to list
                fire.setArea(calculateArea(lat, lon, fire.getCountry()));
                fire.setLat(lat);
                fire.setLon(lon);
                fire.setTime(record.get("acq_time"));
                fire.setBrightness(record.get("brightness"));
                fire.setId(date + ":" + currentFireID++);
                newAllFires.add(fire);
            }
        }
        this.allFires = newAllFires;
        this.ausFires = newAusFires;
        this.nzFires = newNzFires;
        this.piFires = newPiFires;
        allFiresToJson();
    }

    /**
     * Getter methods for values used by the HomeController to add to module.
     */

    public List<Fire> getAllStats() {
        return this.allFires;
    }

    public List<Fire> getAusStats() {
        return this.ausFires;
    }

    public List<Fire> getNzStats() {
        return this.nzFires;
    }

    public List<Fire> getPiStats() {
        return this.piFires;
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
     * The raw fetched data often lists more than one fire for the same area - so
     * this method checks if a given area has already recorded a Fire. eg. (102.764,
     * 24.554) is the same fire as (102.763, 24.552)
     * 
     * @param List<Fire>
     */
    private Boolean fireAlreadyExistsAtCoordinate(String lat, String lon, List<Fire> fires) {
        Double latitudeToCheck = Double.parseDouble(lat);
        Double longitudeToCheck = Double.parseDouble(lon);
        for (Fire fire : fires) {
            Double currentFireLat = Double.parseDouble(fire.getLat());
            Double currentFireLon = Double.parseDouble(fire.getLon());
            // If areas are very close, return true
            if ((Math.abs(currentFireLat - latitudeToCheck) < 0.005)
                    && (Math.abs(currentFireLon - longitudeToCheck) < 0.005)) {
                return true;
            }
        }
        return false;
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
     * 
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
                return this.areaTotals[12]; // Unknown area
        }
    }
     /**
      * Convert all Fire objects into a JSON Array.
      * @return JSON Array
      * @throws JsonProcessingException
      */
    public String allFiresToJson() throws JsonProcessingException {    
        ObjectMapper mapper = new ObjectMapper();
        List<ObjectNode> jsonObjects = new ArrayList<>(); 
        for (Fire fire : this.allFires) { 
         ObjectNode f = mapper.createObjectNode();
         f.put("lat", fire.getLat());
         f.put("lon", fire.getLon());
         jsonObjects.add(f); 
        }
        ArrayNode arrayNode = mapper.createArrayNode();
        arrayNode.addAll(jsonObjects);
        return arrayNode.toString(); 
    }

}
