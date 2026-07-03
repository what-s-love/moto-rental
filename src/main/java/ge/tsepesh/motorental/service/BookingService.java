package ge.tsepesh.motorental.service;

import ge.tsepesh.motorental.dto.DashboardStatsDto;
import ge.tsepesh.motorental.dto.ParticipantAdminDto;
import ge.tsepesh.motorental.dto.ParticipantDto;
import ge.tsepesh.motorental.dto.booking.BookingAdminDto;
import ge.tsepesh.motorental.dto.booking.BookingCreateAdminDto;
import ge.tsepesh.motorental.dto.booking.BookingRequestDto;
import ge.tsepesh.motorental.dto.booking.BookingResponseDto;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookingService {
    //ToDo Зачем передаётся sessionId и как это связано с Redis

    private final ClientService clientService;
    private final RideService rideService;
    private final ParticipantService participantService;
    private final AppSettingService appSettingService;
    private final BookingRepository bookingRepository;
    private final EmailService emailService;
    private final PolicyService policyService;
    private final ConsentService consentService;
    private final YooKassaPaymentService yooKassaPaymentService;

    @Transactional
    public BookingResponseDto createBooking(BookingRequestDto request, String sessionId) {
        log.info("Creating booking for session {} with {} participants", sessionId, request.getParticipants().size());

        // 1. Найти или создать клиента
        Client client = clientService.findOrCreate(request.getClient());
        client.setEmail(request.getClient().getEmail());

        // 2. Найти или создать заезд
        Ride ride = (request.getRouteId() != null)
                ? rideService.findOrCreate(request.getDate(), request.getShiftId(), request.getRouteId())
                : rideService.findOrCreateWithDefaultRoute(request.getDate(), request.getShiftId());

        // 3. Валидация доступности мотоциклов с пессимистической блокировкой
        validateAndLockBikes(request.getParticipants(), ride);

        try {
            // 4. Создать участников
            List<Participant> participants = createParticipants(request.getParticipants(), ride, client);

            // 5. Рассчитать стоимость
            BigDecimal totalPrice = calculateTotalPrice(ride.getRoute(), participants.size(), request.getDate());

            // 6. Создать бронирование
            Booking booking = createBooking(client, ride, totalPrice);

            // 7. Создать соглашение пользователя
            Policy activePolicy = policyService.getActivePolicy();
            consentService.createConsent(client, activePolicy, booking);

            // 8. Освободить резервации в Redis
            //ToDo Настроить работу Redis на проде
//            bikeReservationService.releaseAllReservations(sessionId);

            log.info("Booking {} created successfully for client {}", booking.getId(), client.getEmail());

            // 9. Создание ссылки на оплату в YooKassa (Умный платёж, Redirect)
            String paymentUrl = yooKassaPaymentService.createPrepaymentLink(booking);

            // 10. Отправка пользователю письма с подтверждением и ссылкой на оплату
            emailService.sendPaymentLink(booking, paymentUrl);

            // 11. Отправка уведомления админу в Телеграм-бот
            //ToDo Добавить связку с ТГ-ботом

            return mapToBookingResponse(booking, participants, paymentUrl);

        } catch (Exception e) {
            log.error("Error creating booking for session {}", sessionId, e);
            throw e;
        }
    }

    // Методы для админки
    public Page<BookingAdminDto> getAllBookings(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return bookingRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(this::mapToBookingAdminDto);
    }

    public List<BookingAdminDto> getBookingsByMonth(YearMonth yearMonth) {
        int year = yearMonth.getYear();
        int month = yearMonth.getMonthValue();
        List<Booking> bookings = bookingRepository.findBookingsByYearAndMonth(year, month);
        return bookings.stream()
                .map(this::mapToBookingAdminDto)
                .toList();
    }

    public BookingAdminDto getBookingById(Integer id) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));
        return mapToBookingAdminDto(booking);
    }

    /**
     * Создать бронирование администратором (с возможностью сразу установить статус PAID)
     */
    @Transactional
    public BookingAdminDto createBookingByAdmin(BookingCreateAdminDto dto) {
        log.info("Admin creating booking for date {} shift {} route {}",
                dto.getDate(), dto.getShiftId(), dto.getRouteId());

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
        //ToDo Если PENDING_PAYMENT, то сгенерировать и отобразить ссылку на оплату
        booking.setBookingStatus(Boolean.TRUE.equals(dto.getIsPrepaid())
                ? BookingStatus.PAID
                : BookingStatus.PENDING_PAYMENT);

        booking = bookingRepository.save(booking);

        log.info("Admin created booking {} with status {}", booking.getId(), booking.getBookingStatus());

        return mapToBookingAdminDto(booking);
    }

    @Transactional
    public BookingAdminDto updateBookingStatus(Integer id, BookingStatus newStatus) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found: " + id));

        log.info("Updating booking {} status from {} to {}", id, booking.getBookingStatus(), newStatus);
        booking.setBookingStatus(newStatus);
        booking = bookingRepository.save(booking);

        return mapToBookingAdminDto(booking);
    }

    public DashboardStatsDto getDashboardStats() {
        List<Booking> allBookings = bookingRepository.findAll();

        long totalBookings = allBookings.size();
        long paidCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PAID).count();
        long failedCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PAYMENT_FAILED).count();
        long completedCount = allBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.COMPLETED).count();

        // Текущая неделя (понедельник-воскресенье)
        LocalDate now = LocalDate.now();
        LocalDate startOfWeek = now.with(java.time.DayOfWeek.MONDAY);
        LocalDate endOfWeek = startOfWeek.plusDays(6);

        List<Booking> weekBookings = allBookings.stream()
                .filter(b -> {
                    LocalDate bookingDate = b.getCreatedAt().toLocalDate();
                    return !bookingDate.isBefore(startOfWeek) && !bookingDate.isAfter(endOfWeek);
                })
                .toList();

        long weekTotal = weekBookings.size();
        long weekPending = weekBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PENDING_PAYMENT).count();
        long weekPaid = weekBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PAID).count();
        long weekFailed = weekBookings.stream().filter(b -> b.getBookingStatus() == BookingStatus.PAYMENT_FAILED).count();

        return DashboardStatsDto.builder()
                .totalBookings(totalBookings)
                .paidCount(paidCount)
                .failedCount(failedCount)
                .completedCount(completedCount)
                .weekTotalBookings(weekTotal)
                .weekPendingPaymentCount(weekPending)
                .weekPaidCount(weekPaid)
                .weekFailedCount(weekFailed)
                .build();
    }

    // Вспомогательные методы

    private void validateAndLockBikes(List<ParticipantDto> participantDtos, Ride ride) {
        List<Integer> requestedBikeIds = participantDtos.stream()
                .map(ParticipantDto::getBikeId)
                .toList();

        // Получаем уже занятые байки на эту дату/смену
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

    private Booking createBooking(Client client, Ride ride, BigDecimal totalPrice) {
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

    private BookingResponseDto mapToBookingResponse(Booking booking, List<Participant> participants,
                                                    String paymentUrl) {
        return BookingResponseDto.builder()
                .bookingId(booking.getId())
                .totalPrice(booking.getTotalPrice())
                .currency("RUB")
                .expiresAt(booking.getExpiresAt())
                .status(booking.getBookingStatus())
                .participantCount(participants.size())
                .paymentUrl(paymentUrl)
                .build();
    }

    private BookingAdminDto mapToBookingAdminDto(Booking booking) {
        List<ParticipantAdminDto> participantDtos = participantService
                .findByRideIdOrderByClientName(booking.getRide().getId())
                .stream()
                .map(this::mapToParticipantAdminDto)
                .toList();

        return BookingAdminDto.builder()
                .id(booking.getId())
                .rideDate(booking.getRide().getDate())
                .shiftName(booking.getRide().getShift().getName())
                .startTime(booking.getRide().getShift().getStartTime())
                .endTime(booking.getRide().getShift().getEndTime())
                .routeName(booking.getRide().getRoute().getName())
                .clientName(booking.getClient().getName())
                .clientEmail(booking.getClient().getEmail())
                .clientPhone(booking.getClient().getPhone())
                .totalPrice(booking.getTotalPrice())
                .bookingStatus(booking.getBookingStatus())
                .createdAt(booking.getCreatedAt())
                .expiresAt(booking.getExpiresAt())
                .participants(participantDtos)
                .build();
    }

    private ParticipantAdminDto mapToParticipantAdminDto(Participant p) {
        String bikeName = p.getBike().getBrand() + " " + p.getBike().getModel();
        return ParticipantAdminDto.builder()
                .id(p.getId())
                .bikeName(bikeName)
                .gender(p.getGender() != null ? p.getGender().name() : "N/A")
                .age(p.getAge())
                .height(p.getHeight())
                .experienceLevel(p.getExperienceLevel() != null ? p.getExperienceLevel().name() : "N/A")
                .build();
    }
}























