DROP VIEW IF EXISTS top10_tictactoe;
DROP VIEW IF EXISTS top10_minesweeper;

DROP TABLE IF EXISTS tictactoe_scores;
DROP TABLE IF EXISTS minesweeper_scores;
DROP TABLE IF EXISTS users;


CREATE TABLE users (
    user_id SERIAL PRIMARY KEY,
    username VARCHAR(50) UNIQUE NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password TEXT NOT NULL
);


CREATE TABLE tictactoe_scores (
    user_id INTEGER PRIMARY KEY,
    total_wins INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);

CREATE TABLE minesweeper_scores (
    user_id INTEGER PRIMARY KEY,
    total_wins INTEGER DEFAULT 0,
    best_score INTEGER DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(user_id)
);


CREATE VIEW top10_minesweeper AS
SELECT u.username, ms.best_score
FROM minesweeper_scores ms
         JOIN users u ON u.user_id = ms.user_id
WHERE ms.best_score > 0
ORDER BY ms.best_score ASC
    LIMIT 10;

CREATE VIEW top10_tictactoe AS
SELECT u.username, tts.total_wins
FROM tictactoe_scores tts
         JOIN users u ON u.user_id = tts.user_id
ORDER BY tts.total_wins DESC
    LIMIT 10;



CREATE OR REPLACE FUNCTION check_email_and_username_uniqueness()
RETURNS TRIGGER AS $$
DECLARE
v_email_existent INTEGER;
v_username_existent INTEGER;
BEGIN

SELECT COUNT(*) INTO v_email_existent FROM users WHERE email = NEW.email;
IF v_email_existent > 0 THEN
RAISE EXCEPTION 'Email already exists: %', NEW.email;
END IF;


SELECT COUNT(*) INTO v_username_existent FROM users WHERE username = NEW.username;
IF v_username_existent > 0 THEN
    RAISE EXCEPTION 'Username already exists: %', NEW.username;
END IF;

RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER validate_user_insert
    BEFORE INSERT ON users
    FOR EACH ROW
    EXECUTE FUNCTION check_email_and_username_uniqueness();



