use moviedb;

CREATE TABLE IF NOT EXISTS next_ids (
    table_name VARCHAR(100) PRIMARY KEY,
    next_id INT NOT NULL
);

INSERT INTO next_ids
SELECT 'movies', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1
FROM movies
ON DUPLICATE KEY UPDATE next_id = VALUES(next_id);

INSERT INTO next_ids
SELECT 'stars', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1
FROM stars
ON DUPLICATE KEY UPDATE next_id = VALUES(next_id);

INSERT INTO next_ids
SELECT 'genres', MAX(id) + 1
FROM genres
ON DUPLICATE KEY UPDATE next_id = VALUES(next_id);


DELIMITER //
CREATE PROCEDURE add_star (
    IN name VARCHAR(100),
    IN birthYear INTEGER,
	OUT new_id VARCHAR(10)

)
BEGIN
	SELECT CONCAT('nm', next_id) INTO new_id
	FROM next_ids
	WHERE table_name = 'stars';

    UPDATE next_ids
	SET next_id = next_id + 1
	WHERE table_name = 'stars';

    INSERT INTO stars VALUES (new_id, name, birthYear);

END //
DELIMITER ;


DELIMITER //
CREATE PROCEDURE add_genre (
    IN name VARCHAR(32),
	OUT new_id INT
)
BEGIN
	SELECT next_id INTO new_id
	FROM next_ids
	WHERE table_name = 'genres';

    UPDATE next_ids
	SET next_id = next_id + 1
	WHERE table_name = 'genres';

    INSERT INTO genres VALUES (new_id, name);

END //
DELIMITER ;


DELIMITER //
CREATE PROCEDURE add_movie (
    IN movieTitle VARCHAR(100),
    IN movieYear INTEGER,
    IN movieDirector VARCHAR(100),
    IN starName VARCHAR(100),
	IN starBirthYear INTEGER,
    IN genre VARCHAR(32)
)


BEGIN
-- 	check if movie exists
	DECLARE movie_id VARCHAR(10);
	DECLARE star_id VARCHAR(10);
	DECLARE genre_id INT;

    IF EXISTS (SELECT 1
               FROM movies
               WHERE title = movieTitle AND year = movieYear AND director = movieDirector) THEN
        SELECT 'Movie already exists' AS message;
    ELSE
		SELECT CONCAT('tt0', next_id) INTO movie_id
		FROM next_ids
		WHERE table_name = 'movies';

		UPDATE next_ids
		SET next_id = next_id + 1
		WHERE table_name = 'movies';

		IF EXISTS (SELECT 1 FROM stars WHERE name = starName AND birthYear = starBirthYear) THEN
			SELECT id INTO star_id
			FROM stars
			WHERE name = starName AND birthYear = starBirthYear;
		ELSE
			CALL add_star(starName, starBirthYear, star_id);
		END IF;

		IF EXISTS (SELECT 1 FROM genres WHERE name = genre) THEN
			SELECT id INTO genre_id
			FROM genres
			WHERE name = genre;
		ELSE
			CALL add_genre(genre, genre_id);
		END IF;


		INSERT INTO movies VALUES(movie_id, movieTitle, movieYear, movieDirector);

		INSERT INTO stars_in_movies VALUES (star_id, movie_id);
		INSERT INTO genres_in_movies VALUES (genre_id, movie_id);

		SELECT 'Movie added!' AS message;

    END IF;
END //
DELIMITER ;
