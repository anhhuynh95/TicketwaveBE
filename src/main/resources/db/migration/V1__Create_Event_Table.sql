CREATE TABLE event (
                       id INT PRIMARY KEY AUTO_INCREMENT,
                       name VARCHAR(255) NOT NULL,
                       location VARCHAR(255) NOT NULL,
                       description TEXT,
                       date_time DATETIME NOT NULL,
                       ticket_quantity INT NOT NULL
);
