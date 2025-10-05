INSERT INTO limits (height_min, height_max, age_min, only_men) VALUES
    (165, 300, 16, true),
    (152, 170, 12, false),
    (152, 170, 12, false),
    (140, 155, 9, false);

INSERT INTO bikes (brand, model, engine_cc, limit_id, photo_path) VALUES
    ('Regulmoto', 'Sport-003 Z', 250, 1, NULL),
    ('Roliz', 'Sport - 005', 250, 1, NULL),
    ('Regulmoto', 'FIVE EA', 49, 2, NULL),
    ('Regulmoto', 'SPITFIRE PRO', 49, 2, NULL),
    ('Regulmoto', 'FLY', 49, 2, NULL);