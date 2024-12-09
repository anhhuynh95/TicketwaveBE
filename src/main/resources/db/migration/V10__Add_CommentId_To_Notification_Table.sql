-- Add the column for referencing comment
ALTER TABLE notifications
    ADD COLUMN comment_id INT;

-- Add the foreign key with ON DELETE SET NULL
ALTER TABLE notifications
    ADD CONSTRAINT fk_comment_notification FOREIGN KEY (comment_id)
        REFERENCES comment(id) ON DELETE SET NULL;
