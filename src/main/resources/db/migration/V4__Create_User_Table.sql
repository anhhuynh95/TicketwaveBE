CREATE TABLE `useraccount` (
                        id INT AUTO_INCREMENT PRIMARY KEY,
                        username VARCHAR(50) NOT NULL UNIQUE,
                        password VARCHAR(255) NOT NULL,
                        role ENUM('USER', 'MANAGER', 'ADMIN') NOT NULL DEFAULT 'USER',
                        active BOOLEAN DEFAULT TRUE,
                        created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
