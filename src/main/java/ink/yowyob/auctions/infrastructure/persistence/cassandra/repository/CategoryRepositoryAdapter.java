package ink.yowyob.auctions.infrastructure.persistence.cassandra.repository;

import ink.yowyob.auctions.application.port.out.CategoryRepositoryPort;
import ink.yowyob.auctions.domain.model.Category;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class CategoryRepositoryAdapter implements CategoryRepositoryPort {
    @Override
    public Mono<Category> findById(UUID categoryId) {
        return null;
    }

    @Override
    public Flux<Category> findAll() {
        return null;
    }
}
