ALTER TABLE limits ALTER COLUMN height_min DROP NOT NULL;
ALTER TABLE limits ALTER COLUMN height_max DROP NOT NULL;

UPDATE limits SET height_max = NULL WHERE height_max = 300;