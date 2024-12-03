CREATE TABLE user_behavior (
                               id INT AUTO_INCREMENT PRIMARY KEY,
                               user_id INT NOT NULL,
                               warnings INT DEFAULT 0,
                               is_banned BOOLEAN DEFAULT FALSE,
                               FOREIGN KEY (user_id) REFERENCES useraccount(id) ON DELETE CASCADE
);

ALTER TABLE comment ADD COLUMN is_toxic BOOLEAN DEFAULT FALSE;
