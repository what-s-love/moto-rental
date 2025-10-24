INSERT INTO limits (height_min, height_max, age_min, only_men) VALUES
    (165, 300, 16, true),
    (152, 170, 12, false),
    (140, 155, 9, false);

INSERT INTO bikes (brand, model, engine_cc, transmission_type, limit_id, photo_path, enabled) VALUES
    ('Regulmoto', 'Sport-003 Z', 250, 0, 1, '/images/bikes/sport-003_z_1.jpg', true),
    ('Regulmoto', 'Sport-003 Z', 250, 0, 1, '/images/bikes/sport-003_z_2.jpg', true),
    ('Roliz', 'Sport - 005', 250, 0, 1, '/images/bikes/Roliz-sport-005_1.jpg', true),
    ('Roliz', 'Sport - 005', 250, 0, 1, '/images/bikes/Roliz-sport-005_2.jpg', true),
    ('Regulmoto', 'FIVE EA', 115, 1, 3, '/images/bikes/Regulmoto_FIVE_EA_1.png', true),
    ('Regulmoto', 'FIVE EA', 115, 1, 3, '/images/bikes/Regulmoto_FIVE_EA_2.png', true),
    ('Regulmoto', 'SPITFIRE PRO', 140, 0, 2, '/images/bikes/Regulmoto_SPITFIRE_PRO.png', true),
    ('Regulmoto', 'FLY', 49, 2, 2, '/images/bikes/Regulmoto_FLY.jpg', true);