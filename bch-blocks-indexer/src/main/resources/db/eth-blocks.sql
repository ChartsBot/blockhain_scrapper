CREATE TABLE IF NOT EXISTS EthBlocks (
    number INT NOT NULL,
    blockTimestamp INT NOT NULL,
    INDEX ind_num (number),
    INDEX ind_ts (blockTimestamp),
    CONSTRAINT number_unique UNIQUE (number)
);

CREATE TABLE IF NOT EXISTS PolygonBlocks (
    number INT NOT NULL,
    blockTimestamp INT NOT NULL,
    INDEX ind_num (number),
    INDEX ind_ts (blockTimestamp),
    CONSTRAINT number_unique UNIQUE (number)
);