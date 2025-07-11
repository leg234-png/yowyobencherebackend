//---> PATH: src/main/java/ink/yowyob/auctions/application/port/in/CloseExpiredAuctionsUseCase.java
package ink.yowyob.auctions.application.port.in;

import reactor.core.publisher.Mono;

public interface CloseExpiredAuctionsUseCase {
    /**
     * Finds and closes all auctions that have expired.
     * @return A Mono emitting the number of auctions that were closed.
     */
    Mono<Long> closeExpiredAuctions();
}