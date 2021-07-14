package io.fabianfagan.firetracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.fabianfagan.firetracker.services.FireDataService;

/**
 * Controller for adding data to model and displaying on pages.
 * 
 * @author Fabian Fagan
 */
@Controller
public class HomeController {

    @Autowired
    FireDataService fireDataService;
    

    @GetMapping("/") // root page
    public String home(Model model) {
        // recieve data from data service and add to module
        model.addAttribute("fireStats", fireDataService.getAllStats());
        model.addAttribute("totalNZ", fireDataService.getTotalNzFires());
        model.addAttribute("totalAUS", fireDataService.getTotalAusFires());
        model.addAttribute("totalPI", fireDataService.getTotalPIFires());
        return "home";
    }

    @GetMapping("/AUS") // All AUS info page
    public String aus(Model model) {
        // recieve data from data service and add to module
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
        // recieve data from data service and add to module
        model.addAttribute("nzStats", fireDataService.getNzStats());
        model.addAttribute("totalNZ", fireDataService.getTotalNzFires());
        model.addAttribute("northTotal", fireDataService.getAreaTotal("North Island"));
        model.addAttribute("southTotal", fireDataService.getAreaTotal("South Island"));
        return "nz";
    }

    @GetMapping("/PI") // All PI info page
    public String pi(Model model) {
        // recieve data from data service and add to module
        model.addAttribute("piStats", fireDataService.getPiStats());
        model.addAttribute("totalPi", fireDataService.getTotalPIFires());
        model.addAttribute("newcalTotal", fireDataService.getAreaTotal("New Calidonia/Vanuatu"));
        model.addAttribute("fijiTotal", fireDataService.getAreaTotal("Fiji"));
        model.addAttribute("samoaTotal", fireDataService.getAreaTotal("Samoa"));
        model.addAttribute("tongaTotal", fireDataService.getAreaTotal("Tonga/Niue/Cook Islands"));
        return "pi";
    }

}
