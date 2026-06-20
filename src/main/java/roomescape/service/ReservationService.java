package roomescape.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.dao.PaymentOrderDao;
import roomescape.dao.ReservationDao;
import roomescape.dao.ReservationTimeDao;
import roomescape.dao.ThemeDao;
import roomescape.dao.WaitingDao;
import roomescape.domain.PaymentOrder;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.dto.request.ReservationRequest;
import roomescape.dto.request.UpdateReservationRequest;
import roomescape.dto.response.ReservationResponse;
import roomescape.exception.code.ReservationErrorCode;
import roomescape.exception.code.ReservationTimeErrorCode;
import roomescape.exception.code.ThemeErrorCode;
import roomescape.exception.domain.ReservationException;
import roomescape.exception.domain.ReservationTimeException;
import roomescape.exception.domain.ThemeException;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final Logger log = LoggerFactory.getLogger(ReservationService.class);

    private final SlotService slotService;

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final WaitingDao waitingDao;
    private final PaymentOrderDao paymentOrderDao;

    public ReservationService(SlotService slotService, ReservationDao reservationDao,
                              ReservationTimeDao reservationTimeDao, ThemeDao themeDao,
                              WaitingDao waitingDao, PaymentOrderDao paymentOrderDao) {
        this.slotService = slotService;
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.themeDao = themeDao;
        this.waitingDao = waitingDao;
        this.paymentOrderDao = paymentOrderDao;
    }

    @Transactional
    public ReservationResponse create(ReservationRequest request, LocalDateTime currentDateTime) {
        ReservationTime reservationTime = getTime(request.timeId());
        Theme theme = getTheme(request.themeId());
        Slot slot = slotService.findOrCreate(request.date(), reservationTime, theme);

        Reservation reservation = request.toReservation(slot, currentDateTime);
        validateUniqueReservation(theme.getId(), reservation.getDate(), reservationTime.getId());
        Reservation savedReservation = reservationDao.save(reservation);

        String orderId = generateOrderId();
        PaymentOrder paymentOrder = PaymentOrder.createPending(orderId, request.amount(), savedReservation);
        paymentOrderDao.save(paymentOrder);

        log.info("예약 생성(결제대기): reservationId={}, orderId={}", savedReservation.getId(), orderId);
        return ReservationResponse.from(savedReservation, paymentOrder);
    }

    private String generateOrderId() {
        return "order-" + UUID.randomUUID().toString().replace("-", "");
    }

    private void validateUniqueReservation(long themeId, LocalDate date, long timeId) {
        boolean exists = reservationDao.existsByThemeAndDateAndTime(themeId, date, timeId);
        if (exists) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private ReservationTime getTime(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new ReservationTimeException(ReservationTimeErrorCode.RESERVATION_TIME_NOT_FOUND));
    }

    private Theme getTheme(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new ThemeException(ThemeErrorCode.THEME_NOT_FOUND));
    }

    public List<ReservationResponse> getReservations() {
        List<Reservation> reservations = reservationDao.findAll();
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    public List<ReservationResponse> getReservationsByName(String name) {
        List<Reservation> reservations = reservationDao.findAllByName(name);
        return reservations.stream()
                .map(ReservationResponse::from)
                .toList();
    }

    @Transactional
    public ReservationResponse update(long reservationId, UpdateReservationRequest request, LocalDateTime currentDateTime) {
        Reservation reservation = getReservation(reservationId);
        Slot previousSlot = reservation.getSlot();
        validateModifiable(reservation, currentDateTime);

        ReservationTime reservationTime = getTime(request.timeId());
        validateNotPastDateTime(request.date(), reservationTime, currentDateTime);
        validateUniqueReservationForUpdate(reservation, request.date(), reservationTime);

        Slot newSlot = slotService.findOrCreate(request.date(), reservationTime, reservation.getTheme());
        validateSlotChanged(previousSlot, newSlot);
        Reservation updatedReservation = reservation.updateReservation(newSlot);
        reservationDao.update(updatedReservation);

        promoteFirstWaiting(previousSlot);
        return ReservationResponse.from(updatedReservation);
    }

    private void validateModifiable(Reservation reservation, LocalDateTime currentDateTime) {
        if (reservation.isNotModifiableAt(currentDateTime)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_CANCEL_DEADLINE_PASSED);
        }
    }

    private void validateNotPastDateTime(LocalDate date, ReservationTime time, LocalDateTime now) {
        LocalDateTime reservationDateTime = LocalDateTime.of(date, time.getStartAt());
        if (reservationDateTime.isBefore(now)) {
            throw new ReservationException(ReservationErrorCode.PAST_DATE_NOT_ALLOWED);
        }
    }

    private void validateUniqueReservationForUpdate(Reservation reservation,
                                                    LocalDate date, ReservationTime reservationTime) {
        boolean exists = reservationDao.existsByThemeAndDateAndTimeAndIdNot(
                reservation.getTheme().getId(), date,
                reservationTime.getId(), reservation.getId());
        if (exists) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_ALREADY_EXISTS);
        }
    }

    private void validateSlotChanged(Slot previousSlot, Slot newSlot) {
        if (previousSlot.equals(newSlot)) {
            throw new ReservationException(ReservationErrorCode.RESERVATION_NOT_CHANGED);
        }
    }

    private void promoteFirstWaiting(Slot previousSlot) {
        waitingDao.findFirstBySlot(previousSlot.getId()).ifPresent(waiting -> {
            reservationDao.save(new Reservation(previousSlot, waiting.getName()));
            waitingDao.delete(waiting.getId());
        });
    }

    @Transactional
    public void delete(long reservationId, LocalDateTime currentDateTime) {
        Reservation reservation = getReservation(reservationId);
        validateModifiable(reservation, currentDateTime);
        paymentOrderDao.deleteByReservationId(reservationId);  // FK 제약: reservation 삭제 전에 먼저 지움
        reservationDao.delete(reservationId);

        promoteFirstWaiting(reservation.getSlot());
    }

    private Reservation getReservation(long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new ReservationException(ReservationErrorCode.RESERVATION_NOT_FOUND));
    }
}
