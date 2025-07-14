-- Ajoute une colonne pour suivre le statut du paiement de l'enchère
ALTER TABLE auction
    ADD COLUMN payment_status VARCHAR(50) NOT NULL DEFAULT 'UNPAID' AFTER status;

-- Ajoute un index sur ce nouveau champ
CREATE INDEX idx_auction_payment_status ON auction(payment_status);