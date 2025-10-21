package ge.tsepesh.motorental.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class BikeReservationService {
    //ToDo Почистить комменты
    //ToDo Проверить код
    //ToDo Зачем нужен sessionId

    private final RedisTemplate<String, String> redisTemplate;
    
    private static final String RESERVATION_PREFIX = "bike_reservation:";
    private static final Duration RESERVATION_TTL = Duration.ofMinutes(30);

    public boolean reserveBike(Integer rideId, Integer bikeId, String sessionId) {
        String key = buildReservationKey(rideId, bikeId);
        String value = sessionId + ":" + System.currentTimeMillis();
        
        Boolean success = redisTemplate.opsForValue().setIfAbsent(key, value, RESERVATION_TTL);
        
        if (Boolean.TRUE.equals(success)) {
            log.info("Bike {} reserved for ride {} by session {}", bikeId, rideId, sessionId);
            return true;
        }
        
        log.warn("Failed to reserve bike {} for ride {} by session {}", bikeId, rideId, sessionId);
        return false;
    }

    public void releaseBike(Integer rideId, Integer bikeId, String sessionId) {
        String key = buildReservationKey(rideId, bikeId);
        String currentValue = redisTemplate.opsForValue().get(key);
        
        if (currentValue != null && currentValue.startsWith(sessionId + ":")) {
            redisTemplate.delete(key);
            log.info("Released bike {} for ride {} by session {}", bikeId, rideId, sessionId);
        }
    }

    public Set<String> getReservedBikes(Integer rideId) {
        String pattern = RESERVATION_PREFIX + rideId + ":*";
        return redisTemplate.keys(pattern);
    }

    public void releaseAllReservations(String sessionId) {
        Set<String> allKeys = redisTemplate.keys(RESERVATION_PREFIX + "*");
        
        if (allKeys != null) {
            for (String key : allKeys) {
                String value = redisTemplate.opsForValue().get(key);
                if (value != null && value.startsWith(sessionId + ":")) {
                    redisTemplate.delete(key);
                }
            }
        }
        
        log.info("Released all reservations for session {}", sessionId);
    }

    private String buildReservationKey(Integer rideId, Integer bikeId) {
        return RESERVATION_PREFIX + rideId + ":" + bikeId;
    }
}























