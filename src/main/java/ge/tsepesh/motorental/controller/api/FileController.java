package ge.tsepesh.motorental.controller.api;

import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;

@RestController("RestFiles")
@RequestMapping("/")
@RequiredArgsConstructor
public class FileController {

/*
    @GetMapping("avatar")
    public ResponseEntity<InputStreamResource> getAvatar(Authentication auth) {
        return fileService.getAvatar(auth);
    }
*/

    @GetMapping("css")
    public @ResponseBody byte[] getFile() throws IOException {
        InputStream in = getClass()
                .getResourceAsStream("src/main/resources/static/css/style.css");
        try {
            return in.readAllBytes();

        } catch (Exception e){
            var error = new String("ERROR: css file (resources/static/css/style.css) not found");
            return error.getBytes();
        }
    }

}
