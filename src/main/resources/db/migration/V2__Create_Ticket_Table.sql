CREATE TABLE ticket (
                        id INT PRIMARY KEY AUTO_INCREMENT,
                        event_id INT NOT NULL,
                        ticket_name VARCHAR(100) NOT NULL,
                        price DECIMAL(10, 2) NOT NULL,
                        quantity INT NOT NULL,
                        FOREIGN KEY (event_id) REFERENCES event(id)
);