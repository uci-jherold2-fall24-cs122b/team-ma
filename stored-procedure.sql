use moviedb;

CREATE TABLE IF NOT EXISTS next_ids (
	table_name VARCHAR(100) PRIMARY KEY,
    next_id INT NOT NULL
);

INSERT INTO next_ids
SELECT 'movies', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1
FROM movies
WHERE id REGEXP '^tt[0-9]+$';

INSERT INTO next_ids
SELECT 'stars', IFNULL(MAX(CAST(SUBSTRING(id, 3) AS UNSIGNED)), 0) + 1
FROM stars
WHERE id REGEXP '^nm[0-9]+$';

INSERT INTO next_ids
SELECT 'genres', MAX(id) + 1
FROM genres


DELIMITER //
DROP PROCEDURE IF EXISTS add_star;
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
DROP PROCEDURE IF EXISTS add_genre;

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

DROP PROCEDURE IF EXISTS add_movie;

CREATE PROCEDURE add_movie (
    IN fid VARCHAR(10),
    IN movieTitle VARCHAR(100),
    IN movieYear INTEGER,
    IN movieDirector VARCHAR(100),
    IN starName VARCHAR(100),
    IN starBirthYear INTEGER,
    IN genre VARCHAR(32)
)
BEGIN
    -- Declare variables for IDs
    DECLARE movie_id VARCHAR(10);
    DECLARE star_id VARCHAR(10);
    DECLARE genre_id INT;
    DECLARE star_added BOOLEAN DEFAULT FALSE;
    DECLARE genre_added BOOLEAN DEFAULT FALSE;
    DECLARE message TEXT DEFAULT '';

    -- Check if the movie already exists
    IF EXISTS (SELECT 1
               FROM movies
               WHERE title = movieTitle AND year = movieYear AND director = movieDirector) THEN
SELECT 'Movie already exists.' AS message;
ELSE
        -- Generate a new movie ID
        IF fid IS NOT NULL THEN
			SET movie_id = fid;
ELSE
SELECT CONCAT('tt0', next_id) INTO movie_id
FROM next_ids
WHERE table_name = 'movies';

UPDATE next_ids
SET next_id = next_id + 1
WHERE table_name = 'movies';
END IF;

        -- Check if the star exists, otherwise add the star
		IF starName IS NOT NULL THEN
			IF EXISTS (SELECT 1 FROM stars WHERE name = starName AND (birthYear = starBirthYear OR
            (birthYear IS NULL AND starBirthYear IS NULL))) THEN
			SELECT id INTO star_id
			FROM stars
			WHERE name = starName AND (birthYear = starBirthYear OR (birthYear IS NULL AND starBirthYear IS NULL));

		ELSE
				CALL add_star(starName, starBirthYear, star_id);
				SET star_added = TRUE;
END IF;
END IF;

        -- Check if the genre exists, otherwise add the genre
        IF genre IS NOT NULL THEN
			IF EXISTS (SELECT 1 FROM genres WHERE name = genre) THEN
SELECT id INTO genre_id
FROM genres
WHERE name = genre;
ELSE
				CALL add_genre(genre, genre_id);
				SET genre_added = TRUE;
END IF;
END IF;

        -- Insert the new movie and the associations
INSERT INTO movies VALUES(movie_id, movieTitle, movieYear, movieDirector);

IF starName IS NOT NULL THEN
			INSERT INTO stars_in_movies VALUES (star_id, movie_id);
END IF;

		IF genre is NOT NULL THEN
			INSERT INTO genres_in_movies VALUES (genre_id, movie_id);
END IF;

        -- Generate and display messages with details of the added movie, star, and genre
        SET message = CONCAT(message, 'Movie (id: ', movie_id, ') added! ');

        IF star_added THEN
            SET message = CONCAT(message, 'Star (id: ', star_id, ') added! ');
END IF;

        IF genre_added THEN
            SET message = CONCAT(message, 'Genre (id: ', genre_id, ') added! ');
END IF;

SELECT message AS message;

END IF;
END //
DELIMITER ;


DELIMITER //
DROP PROCEDURE IF EXISTS add_star_in_movie;

CREATE PROCEDURE add_star_in_movie (
    IN stagename VARCHAR(100),
    IN movie_id VARCHAR(10)
)
BEGIN
    DECLARE star_id VARCHAR(10);

SELECT id INTO star_id
FROM stars
WHERE name = stagename
    LIMIT 1;

IF star_id IS NULL THEN
		CALL add_star(stagename, NULL, star_id);
END IF;

    IF movie_id IS NOT NULL THEN
		INSERT INTO stars_in_movies VALUES (star_id, movie_id);

END IF;

END //

DELIMITER ;
