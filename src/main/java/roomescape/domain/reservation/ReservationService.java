package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationSlot;
import roomescape.domain.reservation.dto.MyReservationsResponse;
import roomescape.domain.reservation.dto.ReservationFixRequest;
import roomescape.domain.reservation.dto.ReservationRequest;
import roomescape.domain.reservation.dto.ReservationResponse;
import roomescape.domain.reservationtime.ReservationTime;
import roomescape.domain.reservationtime.ReservationTimeRepository;
import roomescape.domain.reservationtime.ReservationTimes;
import roomescape.domain.reservationtime.dto.TimeResponse;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;
import roomescape.domain.waiting.Waiting;
import roomescape.domain.waiting.WaitingRepository;
import roomescape.domain.waiting.Waitings;
import roomescape.exception.ErrorCode;
import roomescape.exception.RoomescapeException;
import roomescape.payment.PaymentGateway;
import roomescape.payment.dto.PaymentResult;
import roomescape.payment.domain.Payment;
import roomescape.payment.domain.PaymentRepository;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final PaymentRepository paymentRepository;
    private final PaymentGateway paymentGateway;

    @Value("${pending-ttl.minutes:30}")
    private int pendingTtlMinutes;

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository,
            PaymentRepository paymentRepository,
            PaymentGateway paymentGateway
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.paymentRepository = paymentRepository;
        this.paymentGateway = paymentGateway;
    }

    @Transactional
    public ReservationResponse createReservation(ReservationRequest request) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        ReservationSlot slot = ReservationSlot.of(request.date(), time, theme);
        validateDuplicateReservation(slot);

        Reservation reservation = Reservation.of(request.name(), request.date(), time, theme);
        try {
            Reservation saved = reservationRepository.save(reservation);
            return ReservationResponse.from(saved);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void createPendingReservation(ReservationRequest request, String orderId, long quotedAmount) {
        ReservationTime time = reservationTimeRepository.findById(request.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));
        Theme theme = themeRepository.findById(request.themeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.THEME_ID_NOT_FOUND));

        ReservationSlot slot = ReservationSlot.of(request.date(), time, theme);
        validateDuplicateReservation(slot);

        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(pendingTtlMinutes);
        Reservation pending = Reservation.pendingPayment(request.name(), request.date(), time, theme, orderId, quotedAmount, expiresAt);
        try {
            reservationRepository.save(pending);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public ReservationResponse confirmPayment(String orderId, PaymentResult paymentResult) {
        Reservation reservation = reservationRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        reservationRepository.updateStatus(reservation.getId(), ReservationStatus.CONFIRMED);
        paymentRepository.save(Payment.of(
                paymentResult.paymentKey(),
                paymentResult.orderId(),
                paymentResult.totalAmount(),
                paymentResult.status(),
                reservation.getId()
        ));
        return ReservationResponse.from(reservation);
    }

    @Transactional(readOnly = true)
    public List<TimeResponse> getReservations(LocalDate date, Long themeId) {
        ReservationTimes allTimes = ReservationTimes.of(reservationTimeRepository.findAll());
        List<Long> bookedTimeIds = reservationRepository.findTimeByDateAndThemeId(date, themeId);
        return allTimes.availableExcluding(bookedTimeIds).stream()
                .map(TimeResponse::from)
                .toList();
    }

    @Transactional
    public void deleteReservation(Long id) {
        Reservation reservation = reservationRepository.findByIdForUpdate(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));

        paymentRepository.findByReservationId(id).ifPresent(payment -> {
            paymentGateway.cancel(payment.getPaymentKey(), "예약 취소");
            paymentRepository.deleteByReservationId(id);
        });

        int deleted = reservationRepository.deleteById(id);

        if (deleted > 0) {
            Waitings waitings = Waitings.of(waitingRepository.findAllBySlotForUpdate(reservation.getSlot()));
            waitings.first().ifPresent(this::promoteToReservation);
        }
    }

    private void promoteToReservation(Waiting waiting) {
        String orderId = "order-" + UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiresAt = LocalDateTime.now().plusMinutes(pendingTtlMinutes);
        try {
            Reservation pending = Reservation.pendingPayment(
                    waiting.getName(), waiting.getDate(), waiting.getTime(), waiting.getTheme(), orderId,
                    waiting.getTheme().getPrice(), expiresAt
            );
            reservationRepository.save(pending);
            waitingRepository.deleteById(waiting.getId());
        } catch (DuplicateKeyException e) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional(readOnly = true)
    public MyReservationsResponse getMyReservations(String name) {
        return MyReservationsResponse.from(reservationRepository.findByName(name));
    }

    @Transactional
    public void updateMyReservation(Long id, ReservationFixRequest fixRequest) {
        ReservationTime newTime = reservationTimeRepository.findById(fixRequest.timeId())
                .orElseThrow(() -> new RoomescapeException(ErrorCode.TIME_ID_NOT_FOUND));

        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));

        reservation.validateOwner(fixRequest.name());
        validateDuplicateReservation(ReservationSlot.of(fixRequest.date(), newTime, reservation.getTheme()));
        reservation.changeSchedule(fixRequest.date(), newTime);

        try {
            reservationRepository.update(reservation);
        } catch (DuplicateKeyException exception) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    @Transactional
    public void expireAndPromote(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        if (reservation.getStatus() != ReservationStatus.PENDING_PAYMENT) {
            return;
        }
        reservationRepository.deleteById(reservationId);
        Waitings waitings = Waitings.of(waitingRepository.findAllBySlotForUpdate(reservation.getSlot()));
        waitings.first().ifPresent(this::promoteToReservation);
    }

    @Transactional
    public void completeCancelAndCleanup(Long reservationId) {
        Reservation reservation = reservationRepository.findByIdForUpdate(reservationId)
                .orElseThrow(() -> new RoomescapeException(ErrorCode.RESERVATION_ID_NOT_FOUND));
        paymentRepository.deleteByReservationId(reservationId);
        int deleted = reservationRepository.deleteById(reservationId);
        if (deleted > 0) {
            Waitings waitings = Waitings.of(waitingRepository.findAllBySlotForUpdate(reservation.getSlot()));
            waitings.first().ifPresent(this::promoteToReservation);
        }
    }

    private void validateDuplicateReservation(ReservationSlot slot) {
        if (reservationRepository.existsBySlot(slot)) {
            throw new RoomescapeException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
