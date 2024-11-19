ALTER TABLE `purchased_ticket`
    ADD COLUMN `user_id` INT DEFAULT NULL,
    ADD CONSTRAINT `fk_purchased_ticket_user`
    FOREIGN KEY (`user_id`) REFERENCES `user` (`id`)
    ON DELETE SET NULL;
