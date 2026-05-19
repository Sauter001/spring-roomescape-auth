INSERT INTO reservation (id, user_id, date, time_id, theme_id)
VALUES (1, 2, '2021-01-01', 1, 1);
INSERT INTO reservation (id, user_id, date, time_id, theme_id)
VALUES (2, 2, '2021-01-01', 2, 1);
ALTER TABLE reservation
    ALTER COLUMN id RESTART WITH 3;
