CREATE TABLE purchased_ticket (
                                  id INT AUTO_INCREMENT PRIMARY KEY,
                                  ticket_id INT NOT NULL,
                                  purchase_quantity INT NOT NULL,
                                  purchase_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                                  FOREIGN KEY (ticket_id) REFERENCES ticket(id)
);
