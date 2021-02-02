CREATE TABLE IF NOT EXISTS EthBlocks (
    number BIGINT NOT NULL,
    hash VARCHAR(128),
    blockTimestamp INT NOT NULL,
    INDEX ind_num (number),
    INDEX ind_ts (blockTimestamp),
    CONSTRAINT unique_block UNIQUE(number, hash)
);