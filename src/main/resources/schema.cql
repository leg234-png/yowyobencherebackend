//---> PATH: src/main/resources/schema.cql

// Exécutez ce script avec cqlsh: cqlsh -f src/main/resources/schema.cql

CREATE KEYSPACE IF NOT EXISTS enchereyowyob
WITH replication = {'class': 'SimpleStrategy', 'replication_factor': '1'};

USE enchereyowyob;

-- Table pour les catégories, simple lookup.
CREATE TABLE IF NOT EXISTS categories (
    id UUID PRIMARY KEY,
    name TEXT,
    description TEXT
);

-- Table principale pour la recherche par ID. La source de vérité.
CREATE TABLE IF NOT EXISTS auctions (
    id UUID PRIMARY KEY,
    title TEXT,
    description TEXT,
    starting_price DECIMAL,
    current_price DECIMAL,
    start_date TIMESTAMP,
    end_date TIMESTAMP,
    status TEXT,
    agency_id UUID,
    category_id UUID,
    item_condition TEXT,
    image_urls SET<TEXT>,
    participants SET<TEXT>,
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

-- Table dénormalisée pour la recherche par agence.
CREATE TABLE IF NOT EXISTS auctions_by_agency (
    agency_id UUID,
    end_date TIMESTAMP,
    auction_id UUID,
    title TEXT,
    current_price DECIMAL,
    status TEXT,
    first_image_url TEXT,
    PRIMARY KEY (agency_id, end_date, auction_id)
) WITH CLUSTERING ORDER BY (end_date ASC);

-- Table dénormalisée pour la recherche par catégorie.
CREATE TABLE IF NOT EXISTS auctions_by_category (
    category_id UUID,
    end_date TIMESTAMP,
    auction_id UUID,
    title TEXT,
    current_price DECIMAL,
    status TEXT,
    first_image_url TEXT,
    PRIMARY KEY (category_id, end_date, auction_id)
) WITH CLUSTERING ORDER BY (end_date ASC);

-- Table pour les offres, optimisée pour la recherche par enchère.
CREATE TABLE IF NOT EXISTS bids_by_auction (
    auction_id UUID,
    created_at TIMESTAMP,
    bid_id UUID,
    username TEXT,
    price DECIMAL,
    PRIMARY KEY (auction_id, created_at, bid_id)
) WITH CLUSTERING ORDER BY (created_at DESC);