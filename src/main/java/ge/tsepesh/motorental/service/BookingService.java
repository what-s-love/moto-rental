package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.BookingRequestDto;
import ge.tsepesh.motorental.dto.BookingResponseDto;
import ge.tsepesh.motorental.dto.ClientDto;
import ge.tsepesh.motorental.dto.ParticipantDto;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Bike;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.model.Client;
import ge.tsepesh.motorental.model.Participant;
import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.model.Shift;
import ge.tsepesh.motorental.repository.BikeRepository;
import ge.tsepesh.motorental.repository.BookingRepository;
import ge.tsepesh.motorental.repository.ClientRepository;
import ge.tsepesh.motorental.repository.ParticipantRepository;
import ge.tsepesh.motorental.repository.RideRepository;
import ge.tsepesh.motorental.repository.RouteRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    //ToDo Проверить код
    //ToDo Зачем передаётся sessionId и как это связано с Redis
    //ToDo Дописать необходимые DTO

    private final ClientRepository clientRepository;
    private final RideRepository rideRepository;
    private final RouteRepository routeRepository;
    private final ParticipantRepository participantRepository;
    private final BookingRepository bookingRepository;
    private final BikeRepository bikeRepository;
    private final BikeReservationService bikeReservationService;

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request, String sessionId) {
        log.info("Creating booking for session {} with {} participants", sessionId, request.getParticipants().size());
        
        // 1. Найти или создать клиента
        Client client = findOrCreateClient(request.getClient());
        
        // 2. Найти или создать заезд
        Ride ride = findOrCreateRide(request.getDate(), request.getShiftId(), request.getRouteId());
        
        // 3. Валидация доступности мотоциклов с пессимистической блокировкой
        validateAndLockBikes(request.getParticipants(), ride);
        
        try {
            // 4. Создать участников
            List<Participant> participants = createParticipants(request.getParticipants(), ride, client);
            
            // 5. Рассчитать стоимость
            BigDecimal totalPrice = calculateTotalPrice(ride.getRoute(), participants.size());
            
            // 6. Создать бронирование
            Booking booking = createBooking(client, ride, totalPrice);
            
            // 7. Освободить резервации в Redis
            bikeReservationService.releaseAllReservations(sessionId);
            
            log.info("Booking {} created successfully for client {}", booking.getId(), client.getEmail());
            
            return mapToBookingResponse(booking, participants);
            
        } catch (Exception e) {
            log.error("Error creating booking for session {}", sessionId, e);
            throw e;
        }
    }

    private Client findOrCreateClient(ClientDto clientDto) {
        Optional<Client> existingClient = clientRepository.findByEmailOrPhone(
            clientDto.getEmail(), clientDto.getPhone()
        );
        
        if (existingClient.isPresent()) {
            log.info("Found existing client: {}", clientDto.getEmail());
            return existingClient.get();
        }
        
        Client newClient = new Client();
        newClient.setName(clientDto.getName());
        newClient.setEmail(clientDto.getEmail());
        newClient.setPhone(clientDto.getPhone());
        newClient.setTelegramId(clientDto.getTelegramId());
        newClient.setCreatedAt(LocalDateTime.now());
        
        Client savedClient = clientRepository.save(newClient);
        log.info("Created new client: {}", savedClient.getEmail());
        
        return savedClient;
    }

    private Ride findOrCreateRide(LocalDate date, Integer shiftId, Integer routeId) {
        Optional<Ride> existingRide = rideRepository.findByDateAndShift(date, shiftId);
        
        if (existingRide.isPresent()) {
            log.info("Found existing ride for date {} and shift {}", date, shiftId);
            return existingRide.get();
        }
        
        // Создать новый заезд
        Route route = routeRepository.findById(routeId)
            .orElseThrow(() -> new IllegalArgumentException("Route not found: " + routeId));
        
        Shift shift = new Shift();
        shift.setId(shiftId);
        
        Ride newRide = new Ride();
        newRide.setDate(date);
        newRide.setShift(shift);
        newRide.setRoute(route);
        
        Ride savedRide = rideRepository.save(newRide);
        log.info("Created new ride {} for date {} and shift {}", savedRide.getId(), date, shiftId);
        
        return savedRide;
    }

    private void validateAndLockBikes(List<ParticipantDto> participantDtos, Ride ride) {
        // Пессимистическая блокировка на короткое время
        List<Integer> requestedBikeIds = participantDtos.stream()
            .map(ParticipantDto::getBikeId)
            .toList();
        
        // Проверяем, что все мотоциклы доступны
        List<Bike> availableBikes = bikeRepository.findAvailableBikesForDateAndShift(
            null, ride.getDate(), ride.getShift().getId()
        );
        
        List<Integer> availableBikeIds = availableBikes.stream()
            .map(Bike::getId)
            .toList();
        
        for (Integer bikeId : requestedBikeIds) {
            if (!availableBikeIds.contains(bikeId)) {
                throw new IllegalStateException("Bike " + bikeId + " is not available");
            }
        }
    }

    private List<Participant> createParticipants(List<ParticipantDto> participantDtos,
                                               Ride ride, Client client) {
        return participantDtos.stream()
            .map(dto -> createParticipant(dto, ride, client))
            .toList();
    }

    private Participant createParticipant(ParticipantDto dto, Ride ride, Client client) {
        Bike bike = bikeRepository.findById(dto.getBikeId())
            .orElseThrow(() -> new IllegalArgumentException("Bike not found: " + dto.getBikeId()));
        
        Participant participant = new Participant();
        participant.setGender(dto.getGender());
        participant.setAge(dto.getAge());
        participant.setHeight(dto.getHeight());
        participant.setExperienceLevel(dto.getExperienceLevel());
        participant.setRide(ride);
        participant.setBike(bike);
        participant.setClient(client);
        
        return participantRepository.save(participant);
    }

    private BigDecimal calculateTotalPrice(Route route, int participantCount) {
        return route.getPrice().multiply(BigDecimal.valueOf(participantCount));
    }

    private Booking createBooking(Client client, Ride ride, BigDecimal totalPrice) {
        Booking booking = new Booking();
        booking.setClient(client);
        booking.setRide(ride);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusHours(24)); // 24 часа на оплату //ToDo Проставить нужное время
        booking.setTotalPrice(totalPrice);
        booking.setBookingStatus(BookingStatus.PENDING_PAYMENT);
        
        return bookingRepository.save(booking);
    }

    private BookingResponseDto mapToBookingResponse(Booking booking, List<Participant> participants) {
        return BookingResponseDto.builder()
            .bookingId(booking.getId())
            .totalPrice(booking.getTotalPrice())
            .expiresAt(booking.getExpiresAt())
            .status(booking.getBookingStatus())
            .participantCount(participants.size())
            .build();
    }
}









