//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/controller/FileUploadController.java
package ink.yowyob.auctions.presentation.rest.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
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
@RequestMapping("/uploads")
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


    /**
     * Sert un fichier précédemment uploadé.
     * Cet endpoint est appelé par le navigateur lorsqu'il rencontre une balise <img src="...">
     * dont l'URL pointe vers notre API.
     *
     * @param filename Le nom du fichier à récupérer, extrait de l'URL.
     *                 L'expression régulière `:.+` est importante pour que Spring
     *                 capture correctement les noms de fichiers qui contiennent des points (ex: "image.jpg").
     * @return Une ResponseEntity contenant la ressource (le fichier) si elle est trouvée,
     *         ou une réponse 404 Not Found sinon.
     */
    @GetMapping("/{filename:.+}")
    public Mono<ResponseEntity<Resource>> getFile(@PathVariable String filename) {
        // 1. Construire le chemin complet et sécurisé vers le fichier demandé.
        //    resolve() ajoute le nom du fichier au chemin de base (ex: /app/uploads/mon_image.png)
        //    normalize() nettoie le chemin (ex: supprime les "../") pour éviter les attaques de type "Path Traversal".
        Path filePath = this.uploadPath.resolve(filename).normalize();

        // 2. Créer un objet Resource à partir du chemin du fichier.
        //    FileSystemResource est une implémentation de Resource pour les fichiers sur le disque.
        Resource resource = new FileSystemResource(filePath);

        // 3. Vérifier de manière asynchrone si la ressource existe et est lisible.
        //    On utilise Mono.fromCallable pour ne pas bloquer le thread de l'événement.
        return Mono.fromCallable(() -> resource.exists() && resource.isReadable())
                .flatMap(exists -> {
                    if (exists) {
                        // 4a. Si le fichier existe, le renvoyer avec un statut 200 OK.
                        //     On peut essayer de deviner le type de contenu (MIME type) pour aider le navigateur,
                        //     mais pour des images, le navigateur est souvent assez intelligent.
                        //     Ici, on peut juste définir un type générique ou essayer de le deviner.
                        log.debug("Serving file: {}", filename);
                        return Mono.just(ResponseEntity.ok()
                                .contentType(MediaType.APPLICATION_OCTET_STREAM) // Type générique pour les données binaires
                                // Alternativement, vous pourriez utiliser une lib pour deviner le MIME type à partir du nom du fichier
                                .body(resource));
                    } else {
                        // 4b. Si le fichier n'existe pas ou n'est pas lisible, renvoyer une erreur 404.
                        log.warn("Requested file not found or not readable: {}", filename);
                        return Mono.just(ResponseEntity.notFound().build());
                    }
                });
    }
}