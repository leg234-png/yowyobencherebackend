//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/controller/FileUploadController.java
package ink.yowyob.auctions.presentation.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;

@Slf4j
@RestController
@RequestMapping("/api/uploads")
public class FileUploadController {

    private final Path uploadPath;
    private final String baseUrl;

    public FileUploadController(@Value("${app.upload.path}") String path,
                                @Value("${app.upload.base-url}") String baseUrl) {
        this.uploadPath = Paths.get(path);
        this.baseUrl = baseUrl;
        try {
            Files.createDirectories(uploadPath);
        } catch (IOException e) {
            throw new RuntimeException("Could not initialize storage location", e);
        }
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("isAuthenticated()")
    public Mono<ResponseEntity<List<String>>> uploadFiles(@RequestPart("files") Flux<FilePart> filePartFlux) {
        return filePartFlux
                .flatMap(this::saveFile)
                .collectList()
                .map(urls -> ResponseEntity.status(HttpStatus.CREATED).body(urls));
    }

    private Mono<String> saveFile(FilePart filePart) {
        String filename = UUID.randomUUID() + "_" + filePart.filename();
        Path destinationFile = this.uploadPath.resolve(filename).normalize().toAbsolutePath();

        log.info("Uploading file: {} to path: {}", filename, destinationFile);

        return filePart.transferTo(destinationFile)
                .thenReturn(baseUrl + filename)
                .doOnError(e -> log.error("Failed to upload file {}", filename, e));
    }
}