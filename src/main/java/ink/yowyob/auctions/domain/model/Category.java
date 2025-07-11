//---> PATH: src/main/java/ink/yowyob/auctions/domain/model/Category.java
package ink.yowyob.auctions.domain.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@Builder
@AllArgsConstructor
public class Category {
    private final UUID id;
    private final String name;
    private final String description;
}