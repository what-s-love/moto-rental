package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class RideService {
    private final RideRepository rideRepository;
    private final RouteService routeService;
    private final ShiftService shiftService;
    @Transactional
    public Ride findOrCreate(LocalDate date, Integer shiftId, Integer routeId) {
        return rideRepository.findByDateAndShift(date, shiftId)
                .map(existing -> {
                    log.info("Found existing ride for date {} and shift {}", date, shiftId);
                    return existing;
                })
                .orElseGet(() -> {
                    Route route = routeService.getRouteById(routeId);
                    Shift shift = shiftService.getShiftById(shiftId);
                    Ride ride = new Ride();
                    ride.setDate(date);
                    ride.setShift(shift);
                    ride.setRoute(route);
                    Ride saved = rideRepository.save(ride);
                    log.info("Created new ride {} for date {} and shift {}", saved.getId(), date, shiftId);
                    return saved;
                });
    }
}