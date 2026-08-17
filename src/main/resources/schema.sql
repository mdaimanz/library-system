CREATE TABLE IF NOT EXISTS borrower (
    id BINARY(16) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email_address VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS book (
    id BINARY(16) PRIMARY KEY,
    isbn_number VARCHAR(50) NOT NULL,
    title VARCHAR(1000) NOT NULL,
    author VARCHAR(100) NOT NULL,
    borrower_id BINARY(16) NULL,

    CONSTRAINT fk_borrower_id
        FOREIGN KEY (borrower_id)
        REFERENCES borrower(id)
);