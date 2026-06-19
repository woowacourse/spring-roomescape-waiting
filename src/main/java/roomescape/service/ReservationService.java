package roomescape.service;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Schedule;
import roomescape.domain.Theme;
import roomescape.domain.PaymentOrder;
import roomescape.domain.PaymentOrderStatus;
import roomescape.repository.PaymentOrderRepository;
import roomescape.repository.ReservationRepository;
import roomescape.repository.ReservationTimeRepository;
import roomescape.repository.ThemeRepository;
import roomescape.repository.WaitingRepository;
import roomescape.service.exception.BusinessConflictException;
import roomescape.service.exception.ErrorCode;
import roomescape.service.exception.ResourceNotFoundException;
import roomescape.service.dto.ReservationPayment;
import roomescape.service.dto.UserReservation;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private static final long RESERVATION_AMOUNT = 50_000L;

    private final ReservationRepository reservationRepository;
    private final ReservationTimeRepository reservationTimeRepository;
    private final ThemeRepository themeRepository;
    private final WaitingRepository waitingRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final Clock clock;

    @org.springframework.beans.factory.annotation.Autowired
    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository,
            PaymentOrderRepository paymentOrderRepository,
            Clock clock
    ) {
        this.reservationRepository = reservationRepository;
        this.reservationTimeRepository = reservationTimeRepository;
        this.themeRepository = themeRepository;
        this.waitingRepository = waitingRepository;
        this.paymentOrderRepository = paymentOrderRepository;
        this.clock = clock;
    }

    public ReservationService(
            ReservationRepository reservationRepository,
            ReservationTimeRepository reservationTimeRepository,
            ThemeRepository themeRepository,
            WaitingRepository waitingRepository,
            Clock clock
    ) {
        this(reservationRepository, reservationTimeRepository, themeRepository, waitingRepository, null, clock);
    }

    public List<Reservation> findReservations(int page, int size) {
        return reservationRepository.findAll(page, size);
    }

    public List<Reservation> findUserReservations(String name, int page, int size) {
         return reservationRepository.findUserReservations(name, page, size);
    }

    public List<UserReservation> findUserReservationsWithPayments(String name, int page, int size) {
        return findUserReservations(name, page, size).stream()
                .map(reservation -> new UserReservation(
                        reservation,
                        paymentOrderRepository.findByReservationId(reservation.getId())
                                .map(order -> new ReservationPayment(
                                        order.orderId(),
                                        order.status(),
                                        order.paymentKey(),
                                        order.amount()
                                ))
                                .orElse(null)
                ))
                .toList();
    }

    @Transactional
    public Reservation createReservation(String name, LocalDate date, long timeId, long themeId) {
        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Theme theme = themeRepository.findById(themeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.THEME_NOT_FOUND));

        Reservation reservation = Reservation.create(name, new Schedule(date, time, theme), LocalDateTime.now(clock));
        checkDuplicated(reservation);
        Reservation saved = save(reservation);
        savePaymentOrder(saved);
        return saved;
    }

    @Transactional
    public Reservation updateReservation(long id, String name, LocalDate date, long timeId) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        ReservationTime time = reservationTimeRepository.findById(timeId)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_TIME_NOT_FOUND));

        Reservation updated = reservation.changeSchedule(date, time, name, LocalDateTime.now(clock));
        checkDuplicated(updated);
        update(updated);
        return updated;
    }

    @Transactional
    public void deleteUserReservation(long id, String name) {
        Reservation reservation = reservationRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(ErrorCode.RESERVATION_NOT_FOUND));

        reservation.checkCancellable(name, LocalDateTime.now(clock));
        boolean deleted = reservationRepository.delete(reservation);

        if (!deleted) {
            return;
        }
        waitingRepository.findFirstWaitingByScheduleForUpdate(reservation.getSchedule())
                .ifPresent(waiting -> {
                    reservationRepository.save(waiting.toReservation(LocalDateTime.now(clock)));
                    waitingRepository.delete(waiting);
                });
    }

    private void checkDuplicated(Reservation reservation) {
        Schedule schedule = reservation.getSchedule();
        boolean duplicated = reservationRepository.findBySchedule(schedule)
                .filter(found -> !reservation.isSameReservation(found))
                .isPresent();

        if (duplicated) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private Reservation save(Reservation reservation) {
        try {
            return reservationRepository.save(reservation);
        } catch (DuplicateKeyException e) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }

    private void savePaymentOrder(Reservation reservation) {
        if (paymentOrderRepository == null) {
            return;
        }
        String orderId = "order-" + java.util.UUID.randomUUID().toString().replace("-", "");
        String idempotencyKey = java.util.UUID.randomUUID().toString();
        paymentOrderRepository.save(new PaymentOrder(
                orderId,
                reservation.getId(),
                RESERVATION_AMOUNT,
                idempotencyKey,
                null,
                PaymentOrderStatus.PENDING
        ));
    }

    private void update(Reservation reservation) {
        try {
            reservationRepository.update(reservation);
        } catch (DuplicateKeyException e) {
            throw new BusinessConflictException(ErrorCode.DUPLICATE_RESERVATION);
        }
    }
}
