package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.service.BikeService;
import ge.tsepesh.motorental.service.RouteService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final BikeService bikeService;
    private final RouteService routeService;
    
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bikes", bikeService.getAllActiveBikes());
        model.addAttribute("routes", routeService.getAllActiveRoutes());
        return "index";
    }
}
