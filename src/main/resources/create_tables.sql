CREATE TABLE IF NOT EXISTS Transactions(
    transactionId VARCHAR(100) NOT NULL,
    fromAccountId VARCHAR (100),
    toAccountId VARCHAR (100),
    amount DECIMAL (250),
    currency VARCHAR (10) CHECK (currency in ('RUB', 'EUR', 'USD')),
    createdOn TIMESTAMP,
    status VARCHAR (10) CHECK (status in ('FAILED', 'SUCCESS')),
    message VARCHAR (100),
    PRIMARY KEY(transactionId)
);

CREATE TABLE IF NOT EXISTS Accounts(
    accountId VARCHAR(100) NOT NULL,
    username VARCHAR(100) NOT NULL,
    balance DECIMAL (250),
    currency VARCHAR (10) CHECK (currency in ('RUB', 'EUR', 'USD')),
    PRIMARY KEY(accountId, username)
);


