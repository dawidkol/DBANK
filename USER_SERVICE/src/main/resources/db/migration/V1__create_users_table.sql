DROP TABLE IF EXISTS users;
CREATE TABLE users(
    id VARCHAR(255) NOT NULL PRIMARY KEY,
    first_name VARCHAR(100),
    last_name VARCHAR(100),
    email VARCHAR(100) UNIQUE,
    phone VARCHAR(50) UNIQUE,
    password VARCHAR(150),
    date_of_birth DATE,
    created_at TIMESTAMP,
    created_by VARCHAR(100),
    updated_at TIMESTAMP, 
    updated_by VARCHAR(100)
    );

