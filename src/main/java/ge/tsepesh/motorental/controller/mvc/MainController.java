package ge.tsepesh.motorental.controller.mvc;

import ge.tsepesh.motorental.service.BikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@RequiredArgsConstructor
public class MainController {
    private final BikeService bikeService;
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("bikes", bikeService.getAllActiveBikes());
        return "index";
    }
}
