//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/configuration/OpenAPIConfiguration.java
package ink.yowyob.auctions.infrastructure.configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Auction Service API",
                version = "1.0.0",
                description = "This API exposes endpoints for a reactive auction system built with Hexagonal Architecture.",
                contact = @Contact(
                        name = "Bengono Amvela Nathan",
                        email = "nathanamvelabengono@gmail.com",
                        url = "https://www.yowyob.com"
                ),
                license = @License(
                        name = "Apache 2.0",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"
                )
        ),
        servers = @Server(
                url = "${server.url:http://localhost:8031}",
                description = "Development Server"
        ),
        // Applique la sécurité à TOUS les endpoints
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth", // Le même nom que dans le @SecurityRequirement
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "JWT Bearer token"
)
public class OpenAPIConfiguration {
    // Le corps de la classe peut maintenant être vide !
    // Toutes les configurations sont gérées par les annotations ci-dessus.
    // On garde la classe pour que Spring la scanne et lise ses annotations.
}