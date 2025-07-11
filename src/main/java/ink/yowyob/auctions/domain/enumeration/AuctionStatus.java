package ink.yowyob.auctions.domain.enumeration;

public enum AuctionStatus {
    SCHEDULED, // Une enchère planifiée pour le futur
    OPEN,      // Une enchère actuellement en cours
    CLOSED,    // Une enchère terminée
    CANCELLED  // Une enchère annulée par l'organisateur
}