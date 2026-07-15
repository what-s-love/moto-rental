package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.ParticipantDto;
import ge.tsepesh.motorental.dto.booking.BookingCreateAdminDto;
import ge.tsepesh.motorental.dto.booking.BookingRequestDto;
import ge.tsepesh.motorental.enums.AppSettingKey;
import ge.tsepesh.motorental.enums.BookingStatus;
import ge.tsepesh.motorental.model.Booking;
import ge.tsepesh.motorental.model.Client;
import ge.tsepesh.motorental.model.Participant;
import ge.tsepesh.motorental.model.Policy;
import ge.tsepesh.motorental.model.Ride;
import ge.tsepesh.motorental.model.Route;
import ge.tsepesh.motorental.repository.BookingRepository;
import ge.tsepesh.motorental.util.DateUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Transactional service responsible solely for persisting a new booking and its related entities.
 * Extracted from BookingService to avoid @Transactional self-invocation through Spring AOP proxy.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class BookingCreationService {

    private final ClientService clientService;
    private final RideService rideService;
    private final ParticipantService participantService;
    private final AppSettingService appSettingService;
    private final BookingRepository bookingRepository;
    private final PolicyService policyService;
    private final ConsentService consentService;

    @Transactional
    public BookingResult createBookingInDB(BookingRequestDto request) {
        // 1. Найти или создать клиента
        Client client = clientService.findOrCreate(request.getClient());
        client.setEmail(request.getClient().getEmail());

        // 2. Найти или создать заезд
        Ride ride = (request.getRouteId() != null)
                ? rideService.findOrCreate(request.getDate(), request.getShiftId(), request.getRouteId())
                : rideService.findOrCreateWithDefaultRoute(request.getDate(), request.getShiftId());

        // 3. Валидация доступности мотоциклов с пессимистической блокировкой
        validateAndLockBikes(request.getParticipants(), ride);

        // 4. Создать участников
        List<Participant> participants = createParticipants(request.getParticipants(), ride, client);

        // 5. Рассчитать стоимость
        BigDecimal totalPrice = calculateTotalPrice(ride.getRoute(), participants.size(), request.getDate());

        // 6. Создать бронирование
        Booking booking = persistBooking(client, ride, totalPrice);

        // 7. Создать соглашение пользователя
        Policy activePolicy = policyService.getActivePolicy();
        consentService.createConsent(client, activePolicy, booking);

        log.info("Booking {} created successfully for client {}", booking.getId(), client.getEmail());
        return new BookingResult(booking, participants);
    }

    @Transactional
    public Booking createBookingInDbByAdmin(BookingCreateAdminDto dto) {
        // 1. Найти или создать клиента
        Client client = clientService.findOrCreate(dto.getClient());
        client.setEmail(dto.getClient().getEmail());
        // 2. Найти или создать заезд
        Ride ride = rideService.findOrCreate(dto.getDate(), dto.getShiftId(), dto.getRouteId());
        // 3. Валидация доступности мотоциклов
        List<Integer> requestedBikeIds = dto.getParticipants()
                .stream()
                .map(ParticipantDto::getBikeId)
                .toList();

        List<Integer> occupiedBikeIds = participantService.findOccupiedBikeIds(
                ride.getDate(), ride.getShift().getId()
        );

        for (Integer bikeId : requestedBikeIds) {
            if (occupiedBikeIds.contains(bikeId)) {
                throw new IllegalStateException("Bike " + bikeId + " is already occupied");
            }
        }

        // 4. Создать участников
        List<Participant> participants = createParticipants(dto.getParticipants(), ride, client);
        // 5. Рассчитать стоимость
        BigDecimal totalPrice = calculateTotalPrice(ride.getRoute(), participants.size(), dto.getDate());
        // 6. Создать бронирование
        long paymentPeriodHours = Long.parseLong(
                appSettingService.getValueOrDefault(AppSettingKey.PREPAYMENT_PERIOD, "2"));
        Booking booking = new Booking();
        booking.setClient(client);
        booking.setRide(ride);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusHours(paymentPeriodHours));
        booking.setTotalPrice(totalPrice);

        // Если isPrepaid=true — сразу PAID, иначе PENDING_PAYMENT
        booking.setBookingStatus(Boolean.TRUE.equals(dto.getIsPrepaid())
                ? BookingStatus.PAID
                : BookingStatus.PENDING_PAYMENT);

        return bookingRepository.save(booking);
    }

    private void validateAndLockBikes(List<ParticipantDto> participantDtos, Ride ride) {
        List<Integer> requestedBikeIds = participantDtos.stream()
                .map(ParticipantDto::getBikeId)
                .toList();

        List<Integer> occupiedBikeIds = participantService.findOccupiedBikeIds(
                ride.getDate(), ride.getShift().getId()
        );

        for (Integer bikeId : requestedBikeIds) {
            if (occupiedBikeIds.contains(bikeId)) {
                throw new IllegalStateException("Bike " + bikeId + " is not available");
            }
        }
    }

    private List<Participant> createParticipants(List<ParticipantDto> participantDtos,
                                                  Ride ride, Client client) {
        return participantDtos.stream()
                .map(dto -> participantService.create(dto, ride, client))
                .toList();
    }

    private BigDecimal calculateTotalPrice(Route route, int participantCount, LocalDate rideDate) {
        BigDecimal pricePerPerson = DateUtil.isWeekend(rideDate)
                ? route.getWeekendPrice()
                : route.getPrice();
        return pricePerPerson.multiply(BigDecimal.valueOf(participantCount));
    }

    private Booking persistBooking(Client client, Ride ride, BigDecimal totalPrice) {
        long paymentPeriodHours = Long.parseLong(
                appSettingService.getValueOrDefault(AppSettingKey.PREPAYMENT_PERIOD, "2"));

        Booking booking = new Booking();
        booking.setClient(client);
        booking.setRide(ride);
        booking.setCreatedAt(LocalDateTime.now());
        booking.setExpiresAt(LocalDateTime.now().plusHours(paymentPeriodHours));
        booking.setTotalPrice(totalPrice);
        booking.setBookingStatus(BookingStatus.PENDING_PAYMENT);

        return bookingRepository.save(booking);
    }

    public record BookingResult(Booking booking, List<Participant> participants) {}
}
