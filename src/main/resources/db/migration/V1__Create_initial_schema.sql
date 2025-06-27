-- Table des catégories
CREATE TABLE category (
                          id BINARY(16) PRIMARY KEY,
                          name VARCHAR(255) NOT NULL,
                          description TEXT,
                          created_at TIMESTAMP,
                          updated_at TIMESTAMP
);

-- Table des enchères
CREATE TABLE auction (
                         id BINARY(16) PRIMARY KEY,
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         starting_price DECIMAL(19, 2) NOT NULL,
                         current_price DECIMAL(19, 2),
                         start_date TIMESTAMP NOT NULL,
                         end_date TIMESTAMP NOT NULL,
                         status VARCHAR(50) NOT NULL,
                         agency_id BINARY(16) NOT NULL,
                         category_id BINARY(16),
                         image_urls JSON,
                         item_condition VARCHAR(255),
                         created_at TIMESTAMP,
                         updated_at TIMESTAMP,
                         FOREIGN KEY (category_id) REFERENCES category(id)
);

-- Table des offres
CREATE TABLE bid (
                     id BINARY(16) PRIMARY KEY,
                     price DECIMAL(19, 2) NOT NULL,
                     auction_id BINARY(16) NOT NULL,
                     username VARCHAR(255) NOT NULL,
                     created_at TIMESTAMP,
                     updated_at TIMESTAMP,
                     FOREIGN KEY (auction_id) REFERENCES auction(id) ON DELETE CASCADE
);

-- Index pour accélérer les recherches fréquentes
CREATE INDEX idx_auction_status ON auction(status);
CREATE INDEX idx_auction_end_date ON auction(end_date);
CREATE INDEX idx_bid_auction_id ON bid(auction_id);