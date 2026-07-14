package ge.tsepesh.motorental.util;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class FileUtil {
    private static final String UPLOAD_DIR = "data/";

    @SneakyThrows
    public static String saveUploadedFile(MultipartFile file, String subDir) throws IOException {
        // 1. Извлечь только имя файла без каталогов (убирает ../.. атаки)
        String originalFilename = StringUtils.hasText(file.getOriginalFilename())
                ? file.getOriginalFilename() : "file";
        String nameOnly = Paths.get(originalFilename).getFileName().toString();

        // 2. Оставить только безопасные символы; всё остальное → '_'
        String safeName = nameOnly.replaceAll("[^a-zA-Z0-9._-]", "_");

        // 3. Вычленить расширение и привести к нижнему регистру
        String ext = "";
        int dot = safeName.lastIndexOf('.');
        if (dot > 0 && dot < safeName.length() - 1) {
            ext = safeName.substring(dot).toLowerCase();
        }

        // 4. Имя файла = только UUID + расширение (оригинальное имя не попадает на диск)
        String resultFileName = UUID.randomUUID() + ext;

        // 5. Все пути через toAbsolutePath().normalize()
        Path baseDir = Paths.get(UPLOAD_DIR + subDir).toAbsolutePath().normalize();
        Files.createDirectories(baseDir);
        Path filePath = baseDir.resolve(resultFileName).normalize();

        // 6. Контрольная проверка — финальный путь обязан начинаться с baseDir
        if (!filePath.startsWith(baseDir)) {
            throw new SecurityException("Path traversal attempt detected: " + filePath);
        }

        try (OutputStream os = Files.newOutputStream(filePath)) {
            os.write(file.getBytes());
        }
        return filePath.toString();
    }

    public static ResponseEntity<InputStreamResource> getOutputFile(String fileName, String subDir) {
        try {
            //    Базовая директория — абсолютный нормализованный путь
            //    toAbsolutePath() разворачивается относительно WORKDIR /app в контейнере,
            //    то есть "data/" → /app/data/, что и есть смонтированный volume
            Path baseDir = Paths.get(UPLOAD_DIR + subDir).toAbsolutePath().normalize();
            //    Берём только имя файла (без каталогов) и строим путь через resolve
            //    Используем getFileName() как дополнительный барьер на случай,
            //    если вызывающий код передал что-то лишнее
            String safeFileName = Paths.get(fileName).getFileName().toString();
            Path filePath = baseDir.resolve(safeFileName).normalize();

            if (!filePath.startsWith(baseDir)) {
                log.warn("Path traversal attempt blocked: fileName='{}', subDir='{}'", fileName, subDir);
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
            if (!Files.exists(filePath) || !Files.isRegularFile(filePath)) {
                log.warn("File not found or not a regular file: {}", filePath);
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            }

            String contentTypeStr = Files.probeContentType(filePath);
            MediaType mediaType = contentTypeStr != null
                    ? MediaType.parseMediaType(contentTypeStr)
                    : MediaType.APPLICATION_OCTET_STREAM;

            return ResponseEntity.ok()
                    .contentType(mediaType)
                    .body(new InputStreamResource(Files.newInputStream(filePath)));
        } catch (IOException e) {
            log.error("Error reading file: subDir='{}', fileName='{}'", subDir, fileName, e);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
}
