package io.fabianfagan.firetracker.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import io.fabianfagan.firetracker.services.FireDataService;
/**
 * Controller for adding data to model and displaying 
 * on home page.
 * @author Fabian Fagan
 */
@Controller
public class HomeController {

    @Autowired
    FireDataService fireDataService; 

    @GetMapping("/") //root page
    public String home(Model model) {
        //recieve data from data service and add to module
        model.addAttribute("fireStats", fireDataService.getAllStats()); 
        model.addAttribute("totalNZ", fireDataService.getTotalNzFires()); 
        model.addAttribute("totalAUS", fireDataService.getTotalAusFires()); 
        model.addAttribute("totalPI", fireDataService.getTotalPIFires()); 
        return "home"; 
    }
}
