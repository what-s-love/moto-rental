package ge.tsepesh.motorental.controller.mvc;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@ControllerAdvice
public class LocaleUrlAdvice {

    @ModelAttribute("langRuUrl")
    public String langRuUrl() {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("lang", "ru")
                .toUriString();
    }

    @ModelAttribute("langEnUrl")
    public String langEnUrl() {
        return ServletUriComponentsBuilder.fromCurrentRequest()
                .replaceQueryParam("lang", "en")
                .toUriString();
    }
}
