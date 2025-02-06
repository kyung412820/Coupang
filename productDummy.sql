DELIMITER $$

DROP PROCEDURE IF EXISTS loopInsert$$

CREATE PROCEDURE loopInsert()
BEGIN
    DECLARE i INT DEFAULT 1;
    DECLARE user_id INT;

    WHILE i <= 5000000 DO

        SET user_id = 1;

INSERT INTO product (user_id, product_name, contents, price)
VALUES (user_id,
        CONCAT('Product ', i),
        CONCAT('This is the description for Product ', i),
        (i * 100));

SET i = i + 1;
END WHILE;
END$$

DELIMITER $$

CALL loopInsert;
