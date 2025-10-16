package ge.tsepesh.motorental.controller.api;

import ge.tsepesh.motorental.util.FileUtil;
import lombok.RequiredArgsConstructor;
//import org.springframework.security.core.Authentication;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;

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

    @GetMapping("images/bikes/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getBikeImage(
            @PathVariable String fileName
    ) {
        String safeName = Paths.get(fileName).getFileName().toString();
        return FileUtil.getOutputFile(safeName, "images/bikes");
    }

    @GetMapping("images/promo/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getPromoImage(
            @PathVariable String fileName
    ) {
        String safeName = Paths.get(fileName).getFileName().toString();
        return FileUtil.getOutputFile(safeName, "images/promo");
    }

    @GetMapping("images/routes/{fileName:.+}")
    public ResponseEntity<InputStreamResource> getRouteImage(
            @PathVariable String fileName
    ) {
        String safeName = Paths.get(fileName).getFileName().toString();
        return FileUtil.getOutputFile(safeName, "images/routes");
    }
}
