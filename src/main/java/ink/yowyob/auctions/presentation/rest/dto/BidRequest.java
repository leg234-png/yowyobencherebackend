//---> PATH: src/main/java/ink/yowyob/auctions/presentation/rest/dto/BidRequest.java
package ink.yowyob.auctions.presentation.rest.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class BidRequest {

    @NotNull(message = "Bid price is mandatory.")
    @Positive(message = "Bid price must be positive.")
    private BigDecimal price;
}