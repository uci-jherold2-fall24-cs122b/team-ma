use moviedb;
DROP PROCEDURE add_star;
DELIMITER //
CREATE PROCEDURE add_star (
    IN name VARCHAR(100),
    IN birthYear INTEGER
)
BEGIN
    DECLARE max_id INT;
    DECLARE new_id VARCHAR(10);
SELECT IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) INTO max_id FROM STARS;
SET new_id = CONCAT('nm', max_id + 1);
INSERT INTO STARS VALUES (new_id, name, birthYear);

END //
DELIMITER ;
