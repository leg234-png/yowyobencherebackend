//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/controller/CategoryController.java
package ink.yowyob.auctions.presentation.rest.controller;

import ink.yowyob.auctions.application.port.in.FindAuctionUseCase;
import ink.yowyob.auctions.application.port.out.CategoryRepositoryPort;
import ink.yowyob.auctions.presentation.rest.dto.AuctionResponse;
import ink.yowyob.auctions.presentation.rest.dto.CategoryResponse;
import ink.yowyob.auctions.presentation.rest.mapper.AuctionRestMapper;
import ink.yowyob.auctions.presentation.rest.mapper.CategoryRestMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

import java.util.UUID;

@RestController
@RequestMapping("/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final CategoryRepositoryPort categoryRepository; // Peut être appelé directement si pas de logique complexe
    private final FindAuctionUseCase findAuctionUseCase;
    @Qualifier("categoryRestMapper")
    private final CategoryRestMapper categoryMapper;
    @Qualifier("auctionRestMapper")
    private final AuctionRestMapper auctionMapper;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public Flux<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .map(categoryMapper::toResponse);
    }

    @GetMapping("/{categoryId}/auctions")
    @ResponseStatus(HttpStatus.OK)
    public Flux<AuctionResponse> getAuctionsByCategory(@PathVariable UUID categoryId) {
        return findAuctionUseCase.findAuctionsByCategory(categoryId)
                .map(auctionMapper::toResponse);
    }
}