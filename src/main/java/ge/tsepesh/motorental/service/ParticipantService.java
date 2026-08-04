package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.ParticipantDto;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Bike;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.model.Client;
import ge.tsepesh.motorental.model.Participant;
import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.repository.ParticipantRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipantService {
    private final ParticipantRepository participantRepository;
    private final BikeService bikeService;

    public List<Integer> findOccupiedBikeIds(LocalDate date, Integer shiftId) {
        return participantRepository.findOccupiedBikeIds(date, shiftId, BookingStatus.ACTIVE_STATUSES);
    }

    public long countByDateAndShift(LocalDate date, Integer shiftId) {
        return participantRepository.countByDateAndShift(date, shiftId, BookingStatus.ACTIVE_STATUSES);
    }

    public List<Participant> findByBookingIdOrderByClientName(Integer bookingId) {
        return participantRepository.findByBookingIdOrderByClientName(bookingId);
    }

    @Transactional
    public Participant create(ParticipantDto dto, Ride ride, Client client, Booking booking) {
        Bike bike = bikeService.getBikeEntityById(dto.getBikeId());
        Participant participant = new Participant();
        participant.setGender(dto.getGender());
        participant.setAge(dto.getAge());
        participant.setHeight(dto.getHeight());
        participant.setExperienceLevel(dto.getExperienceLevel());
        participant.setRide(ride);
        participant.setBike(bike);
        participant.setClient(client);
        participant.setBooking(booking);
        return participantRepository.save(participant);
    }
}