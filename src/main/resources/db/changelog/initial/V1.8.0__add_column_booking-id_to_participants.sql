-- Добавляем внешний ключ
ALTER TABLE participants ADD COLUMN booking_id INT;
ALTER TABLE participants
    ADD CONSTRAINT fk_participant_booking
        FOREIGN KEY (booking_id) REFERENCES bookings(id);

-- Бэкфилл для существующих данных (сопоставление по ride_id + client_id)
UPDATE participants p
SET booking_id = (
    SELECT b.id FROM bookings b
    WHERE b.ride_id = p.ride_id
      AND b.client_id = p.client_id
    ORDER BY b.created_at DESC
    LIMIT 1
    );