DELIMITER //
CREATE PROCEDURE add_star (
    IN name VARCHAR(100),
    IN birthYear INTEGER
)
BEGIN
    DECLARE max_id INT;
SELECT IFNULL(MAX(id), 0) INTO max_id FROM STARS;
SET new_id = max_id + 1;
INSERT INTO STARS (id, starname, birthyear) VALUES (new_id, star_name, birth_year);

END //
DELIMITER ;
