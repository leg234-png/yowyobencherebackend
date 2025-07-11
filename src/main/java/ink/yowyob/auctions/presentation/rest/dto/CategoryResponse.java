//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/dto/CategoryResponse.java
package ink.yowyob.auctions.presentation.rest.dto;

import lombok.Builder;
import lombok.Data;

import java.util.UUID;

@Data
@Builder
public class CategoryResponse {
    private UUID id;
    private String name;
    private String description;
}