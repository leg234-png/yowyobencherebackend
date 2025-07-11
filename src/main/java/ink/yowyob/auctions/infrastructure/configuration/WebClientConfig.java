//---> PATH: src/main/java/ink/yowyob/auctions/infrastructure/configuration/WebClientConfig.java
package ink.yowyob.auctions.infrastructure.configuration;

import io.netty.channel.ChannelOption;
import io.netty.handler.timeout.ReadTimeoutHandler;
import io.netty.handler.timeout.WriteTimeoutHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Configuration
public class WebClientConfig {

    @Bean
    public WebClient.Builder webClientBuilder() {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000) // 5 secondes de timeout pour la connexion
                .responseTimeout(Duration.ofSeconds(10)) // 10 secondes de timeout pour la réponse
                .doOnConnected(connection ->
                        connection.addHandlerLast(new ReadTimeoutHandler(10, TimeUnit.SECONDS))
                                  .addHandlerLast(new WriteTimeoutHandler(10, TimeUnit.SECONDS)));

        return WebClient.builder()
                .clientConnector(new ReactorClientHttpConnector(httpClient));
                // Si vous avez besoin de propager le token JWT, vous pouvez ajouter un ExchangeFilterFunction ici.
                // .filter(new TokenPropagationFilter())
    }
}