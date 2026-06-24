package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Map;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.payment.PaymentRepository;
import roomescape.domain.payment.ReservationPayment;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.reservationtime.dto.TimeSlot;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;

@Service
public class ReservationService {

    private static final long RESERVATION_PRICE = 10_000L;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final PaymentRepository paymentRepository;

    public ReservationService(
        ReservationRepository reservationRepository,
        ReservationTimeRepository reservationTimeRepository,
        ThemeRepository themeRepository,
        WaitingRepository waitingRepository,
        PaymentRepository paymentRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.paymentRepository = paymentRepository;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        validateDuplicateReservation(request.date(), request.timeId(), request.themeId());
        time.validateIfTimePast(request.date());

        Reservation reservation = Reservation.of(
            request.name(),
            request.date(),
            time,
            theme
        );

        try {
            Reservation saved = reservationRepository.save(reservation);
            ReservationPayment payment = createPendingPayment(saved.getId());
            return ReservationResponse.from(saved, payment);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional(readOnly = true)
    public List<TimeResponse> getReservations(LocalDate date, Long themeId) {
        List<ReservationTime> reservationTimes = reservationTimeRepository.findAll();
        List<Long> bookedTimeIds = reservationRepository.findTimeByDateAndThemeId(date, themeId);

        return reservationTimes.stream()
            .filter(reservationTime -> !bookedTimeIds.contains(reservationTime.getId()))
            .map(TimeResponse::from)
            .toList();
    }

    @Transactional
    public void deleteReservation(Long id, String name) {
        reservationRepository.findByIdForUpdate(id)
            .ifPresent(reservation -> {
                reservation.validateOwner(name);
                reservationRepository.deleteById(id);
                promoteFirstWaiting(reservation);
            });
    }

    @Transactional(readOnly = true)
    public MyReservationsResponse getMyReservations(String name) {
        List<Reservation> reservations = reservationRepository.findByName(name);
        List<Long> reservationIds = reservations.stream()
            .map(Reservation::getId)
            .toList();
        Map<Long, ReservationPayment> paymentsByReservationId =
            paymentRepository.findByReservationIds(reservationIds).stream()
                .collect(Collectors.toMap(ReservationPayment::reservationId, Function.identity()));
        return MyReservationsResponse.from(reservations, paymentsByReservationId);
    }

    @Transactional
    public void updateMyReservation(Long id, ReservationFixRequest fixRequest) {
        ReservationTime newTime = reservationTimeRepository.findById(fixRequest.timeId())
            .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        newTime.validateIfTimePast(fixRequest.date());

        if (!reservationRepository.existsByIdForUpdate(id)) {
            throw new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND);
        }
        Reservation reservation = reservationRepository.findById(id)
            .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        validateDuplicateReservation(fixRequest.date(), fixRequest.timeId(), reservation.getTheme().getId());

        reservation.validateOwner(fixRequest.name());

        try {
            reservationRepository.updateDateAndTime(id, fixRequest.date(), fixRequest.timeId());
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void validateDuplicateReservation(LocalDate date, Long timeId, Long themeId) {
        boolean isDuplicated = reservationRepository.existsByDateAndTimeIdAndThemeId(date, timeId, themeId);
        if (isDuplicated) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void promoteFirstWaiting(Reservation reservation) {
        TimeSlot canceledReservationSlot = TimeSlot.from(reservation);

        waitingRepository.findFirstByTimeSlotForUpdate(canceledReservationSlot)
            .ifPresent(waiting -> {
                Reservation promotedReservation = reservationRepository.save(Reservation.of(
                    waiting.getName(),
                    waiting.getDate(),
                    waiting.getTime(),
                    waiting.getTheme()
                ));
                createPendingPayment(promotedReservation.getId());
                waitingRepository.deleteById(waiting.getId());
            });
    }

    private ReservationPayment createPendingPayment(Long reservationId) {
        return paymentRepository.save(ReservationPayment.pending(reservationId, RESERVATION_PRICE));
    }
}
