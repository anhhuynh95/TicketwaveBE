CREATE TABLE comment (
                         id INT AUTO_INCREMENT PRIMARY KEY,
                         event_id INT NOT NULL,
                         user_id INT NOT NULL,
                         comment_text TEXT NOT NULL,
                         created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
                         FOREIGN KEY (event_id) REFERENCES event(id),
                         FOREIGN KEY (user_id) REFERENCES useraccount(id)
);
