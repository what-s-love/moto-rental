package ge.tsepesh.motorental.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * Прокидывает атрибуты, нужные во всех Thymeleaf-шаблонах (og:url, canonical),
 * в модель каждого запроса, обрабатываемого MVC-контроллером — без повторения
 * {@code model.addAttribute(...)} в каждом методе.
 */
@ControllerAdvice
public class GlobalModelAttributes {

    @Value("${app.base-url}")
    private String appBaseUrl;

    @ModelAttribute("appBaseUrl")
    public String appBaseUrl() {
        return appBaseUrl;
    }
}
