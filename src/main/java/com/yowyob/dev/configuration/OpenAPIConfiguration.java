package com.yowyob.dev.configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.servers.Server;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
public class OpenAPIConfiguration {

    @Bean
    public OpenAPI defineOpenApi() {
        Server server = new Server();
        server.setUrl("http://157.90.26.3:8031");
        server.setDescription("Development");

        Contact myContact = new Contact();
        myContact.setName("Bengono Amvela Nathan");
        myContact.setEmail("nathanamvelabengono@gmail.com");

        Info information = new Info()
                .title("Auctions Web Site API")
                .version("1.0")
                .description("This API exposes endpoints for auctions web site.")
                .contact(myContact);
        return new OpenAPI().info(information).servers(List.of(server));
    }
}
