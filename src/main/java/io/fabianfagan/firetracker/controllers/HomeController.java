package io.fabianfagan.firetracker.controllers;

import java.util.List;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import io.fabianfagan.firetracker.modules.Fire;
import io.fabianfagan.firetracker.services.FireDataService;

/**
 * Controller for the home page and specific country pages. 
 * 
 * @author Fabian Fagan
 */
@Controller 
public class HomeController { 

    @Autowired
    FireDataService fireDataService;
     

    @GetMapping("/") // root page
    public String home(Model model) {
        // receive data from data service and add to model
        model.addAttribute("fireStats", fireDataService.getAllStats());
        model.addAttribute("totalNZ", fireDataService.getTotalNzFires());
        model.addAttribute("totalAUS", fireDataService.getTotalAusFires());
        model.addAttribute("totalPI", fireDataService.getTotalPIFires());
        return "home";
    }

    @GetMapping("/AUS") // All AUS info page
    public String aus(Model model) {
        // receive data from data service and add to model
        model.addAttribute("ausStats", fireDataService.getAusStats());
        model.addAttribute("totalAUS", fireDataService.getTotalAusFires());
        model.addAttribute("westernTotal", fireDataService.getAreaTotal("Western Australia"));
        model.addAttribute("northernTotal", fireDataService.getAreaTotal("Northern Territory"));
        model.addAttribute("southTotal", fireDataService.getAreaTotal("South Australia"));
        model.addAttribute("queensTotal", fireDataService.getAreaTotal("Queensland"));
        model.addAttribute("nswTotal", fireDataService.getAreaTotal("NSW"));
        model.addAttribute("tasTotal", fireDataService.getAreaTotal("Tasmania"));
        return "aus";
    }

    @GetMapping("/NZ") // All NZ info page
    public String nz(Model model) {
        // receive data from data service and add to model
        model.addAttribute("nzStats", fireDataService.getNzStats());
        model.addAttribute("totalNZ", fireDataService.getTotalNzFires());
        model.addAttribute("northTotal", fireDataService.getAreaTotal("North Island"));
        model.addAttribute("southTotal", fireDataService.getAreaTotal("South Island"));
        return "nz";
    }

    @GetMapping("/PI") // All PI info page
    public String pi(Model model) {
        // receive data from data service and add to model
        model.addAttribute("piStats", fireDataService.getPiStats());
        model.addAttribute("totalPi", fireDataService.getTotalPIFires());
        model.addAttribute("newcalTotal", fireDataService.getAreaTotal("New Calidonia/Vanuatu"));
        model.addAttribute("fijiTotal", fireDataService.getAreaTotal("Fiji"));
        model.addAttribute("samoaTotal", fireDataService.getAreaTotal("Samoa"));
        model.addAttribute("tongaTotal", fireDataService.getAreaTotal("Tonga/Niue/Cook Islands"));
        return "pi";
    }

    @GetMapping("/{id}") // Single fire info print page
    public ResponseEntity<String> info(Model model, @PathVariable String id) {
        //Return the fire info based on ID 
        List<Fire> allFires = fireDataService.getAllStats();
        Fire requestedFire = new Fire(); 
        for (Fire fire : allFires) {
            if (fire.getId().equals(id)) {
                requestedFire = fire; 
            }
        }
        return ResponseEntity.status(HttpStatus.OK).body(requestedFire.toString());
    }
}
