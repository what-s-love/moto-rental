DROP INDEX IF EXISTS idx_participants_ride_bike;

CREATE INDEX idx_participants_ride_id ON participants(ride_id);
CREATE INDEX idx_participants_bike_id ON participants(bike_id);