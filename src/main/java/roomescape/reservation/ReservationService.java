package roomescape.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.payment.domain.PaymentOrder;
import roomescape.payment.domain.PaymentOrderRepository;
import roomescape.reservation.dto.ReservationResponse;
import roomescape.reservation.exception.ReservationAlreadyExistsException;
import roomescape.reservation.exception.ReservationNotFoundException;
import roomescape.reservationtime.ReservationTime;
import roomescape.reservationtime.ReservationTimeDao;
import roomescape.reservationtime.exception.ReservationTimeNotFoundException;
import roomescape.reservationwait.ReservationWaitDao;
import roomescape.member.Member;
import roomescape.reservationhistory.ReservationHistoryService;

@Service
@Transactional(readOnly = true)
public class ReservationService {

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ReservationWaitDao reservationWaitDao;
    private final ReservationHistoryService reservationHistoryService;
    private final PaymentOrderRepository paymentOrderRepository;

    public ReservationService(ReservationDao reservationDao, ReservationTimeDao reservationTimeDao,
                              ReservationWaitDao reservationWaitDao,
                              ReservationHistoryService reservationHistoryService,
                              PaymentOrderRepository paymentOrderRepository) {
        this.reservationDao = reservationDao;
        this.reservationTimeDao = reservationTimeDao;
        this.reservationWaitDao = reservationWaitDao;
        this.reservationHistoryService = reservationHistoryService;
        this.paymentOrderRepository = paymentOrderRepository;
    }

    public List<Reservation> getReservations(Long memberId) {
        return reservationDao.findAllReservationsByMemberId(memberId);
    }

    public List<ReservationResponse> getReservationsWithPayments(Long memberId) {
        List<Reservation> reservations = reservationDao.findAllReservationsByMemberId(memberId);
        List<Long> reservationIds = reservations.stream().map(Reservation::getId).toList();
        Map<Long, PaymentOrder> byReservationId = paymentOrderRepository.findAllByReservationIdIn(reservationIds)
                .stream()
                .collect(Collectors.toMap(PaymentOrder::getReservationId, Function.identity()));
        return reservations.stream()
                .map(r -> ReservationResponse.from(r, byReservationId.get(r.getId())))
                .toList();
    }

    public List<Reservation> findReservationsByStoreId(Long storeId) {
        return reservationDao.findReservationsByStoreId(storeId);
    }

    @Transactional
    public Reservation createReservation(Long memberId, LocalDate date, Long timeId, Long themeId, Long storeId) {
        ReservationTime time = findReservationTime(timeId);
        Reservation candidate = Reservation.create(memberId, date, time, themeId, storeId);
        try {
            Reservation saved = reservationDao.insert(candidate);
            reservationHistoryService.recordCreated(saved, memberId);
            return saved;
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public Reservation updateReservation(Long id, LocalDate date, Long memberId, Long timeId) {
        Reservation existing = findReservation(id);
        ReservationTime newTime = findReservationTime(timeId);
        Reservation updated = existing.changeTo(memberId, date, newTime);
        try {
            Reservation saved = reservationDao.update(updated);
            reservationHistoryService.recordUpdated(saved, memberId);
            return saved;
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public Reservation updateReservationByManager(Long reservationId, LocalDate date, Long timeId, Member manager) {
        Reservation existing = findReservation(reservationId);
        ReservationTime newTime = findReservationTime(timeId);
        Reservation updated = existing.changeToByManager(manager, date, newTime);
        try {
            Reservation saved = reservationDao.update(updated);
            reservationHistoryService.recordUpdated(saved, manager.getId());
            return saved;
        } catch (DuplicateKeyException e) {
            throw new ReservationAlreadyExistsException();
        }
    }

    @Transactional
    public void deleteReservation(Long reservationId, Long memberId) {
        Reservation reservation = findReservation(reservationId);
        reservation.cancelBy(memberId);
        deleteOrPromoteWait(reservation, memberId);
    }

    @Transactional
    public void deleteReservationByManager(Long reservationId, Member manager) {
        Reservation reservation = findReservation(reservationId);
        reservation.validateStoreOwnership(manager);
        deleteOrPromoteWait(reservation, manager.getId());
    }

    private void deleteOrPromoteWait(Reservation reservation, Long actorId) {
        if (!reservation.isConfirmed()) {
            reservationWaitDao.deleteAllByReservationId(reservation.getId());
            paymentOrderRepository.deleteByReservationId(reservation.getId());
            reservationDao.delete(reservation.getId());
            return;
        }
        if (reservation.isPast()) {
            reservationWaitDao.deleteAllByReservationId(reservation.getId());
            reservationDao.delete(reservation.getId());
            reservationHistoryService.recordCanceled(reservation, actorId);
            return;
        }
        reservationWaitDao.findEarliestMemberIdForUpdate(reservation.getId())
                .ifPresentOrElse(
                        waiterId -> promote(reservation, waiterId, actorId),
                        () -> {
                            reservationDao.delete(reservation.getId());
                            reservationHistoryService.recordCanceled(reservation, actorId);
                        });
    }

    private void promote(Reservation reservation, Long waiterId, Long actorId) {
        Reservation promoted = reservation.promoteTo(waiterId);
        reservationDao.update(promoted);
        reservationWaitDao.deleteByReservationIdAndMemberId(reservation.getId(), waiterId);
        reservationHistoryService.recordTransfer(reservation, promoted, actorId);
    }

    private Reservation findReservation(Long id) {
        try {
            return reservationDao.findReservationByIdForUpdate(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationNotFoundException();
        }
    }

    private ReservationTime findReservationTime(Long id) {
        try {
            return reservationTimeDao.findReservationTimeById(id);
        } catch (EmptyResultDataAccessException e) {
            throw new ReservationTimeNotFoundException();
        }
    }
}
