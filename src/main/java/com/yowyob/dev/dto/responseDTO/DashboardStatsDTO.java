package com.yowyob.dev.dto.responseDTO;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardStatsDTO {
    // Statistiques générales
    private long totalActiveAuctions;
    private long totalClosedAuctions;
    private long totalBidsPlaced;

    // Statistiques de l'utilisateur connecté
    private long myCreatedAuctions;
    private long myParticipationsCount;
    private long myWonAuctionsCount;
}