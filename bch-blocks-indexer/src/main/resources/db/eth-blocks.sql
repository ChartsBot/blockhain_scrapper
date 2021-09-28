CREATE TABLE IF NOT EXISTS EthBlocks (
    number INT NOT NULL,
    blockTimestamp INT NOT NULL,
    INDEX ind_num (number),
    INDEX ind_ts (blockTimestamp)
);

CREATE TABLE IF NOT EXISTS PolygonBlocks (
    number INT NOT NULL,
    blockTimestamp INT NOT NULL,
    INDEX ind_num (number),
    INDEX ind_ts (blockTimestamp)
);