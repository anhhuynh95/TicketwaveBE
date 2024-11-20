ALTER TABLE purchased_ticket
    ADD COLUMN user_id INT DEFAULT NULL;

ALTER TABLE purchased_ticket
    ADD CONSTRAINT fk_purchased_ticket_user
        FOREIGN KEY (user_id) REFERENCES useraccount (id);
