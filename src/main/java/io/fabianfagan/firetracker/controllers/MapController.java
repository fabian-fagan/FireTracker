package io.fabianfagan.firetracker.controllers;

import java.util.List;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.fabianfagan.firetracker.modules.Fire;
import io.fabianfagan.firetracker.services.FireDataService;


/**
 * MapController determines the map to display based on the selected Fire.
 * A default map can also be accessed with '/map'.
 * @author Fabian Fagan
 */
@Controller 
public class MapController {

    @Autowired
    FireDataService fireDataService;

    @GetMapping("/map/{id}") // Single fire map view page
    public String map(Model model, @PathVariable String id) throws JsonProcessingException {
        //Find requested fire
        List<Fire> allFires = fireDataService.getAllStats();
        Fire requestedFire = new Fire(); 
        for (Fire fire : allFires) {
            if (fire.getId().equals(id)) {
                requestedFire = fire; 
            }
        }
        //Add to model      
        model.addAttribute("requestedFireLat", requestedFire.getLat()); 
        model.addAttribute("requestedFireLon", requestedFire.getLon());
        return "map"; 
    }

    @GetMapping("/map") // Default map page (No ID given)
    public String mapRoot(Model model) throws JsonProcessingException {
        //Add to model      
        model.addAttribute("requestedFireLat", -36.8307293); 
        model.addAttribute("requestedFireLon", 174.7456027);
        return "map"; 
    }
    
}
