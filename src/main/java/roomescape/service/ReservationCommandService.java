package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationStatus;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import org.springframework.context.ApplicationEventPublisher;
import payment.ReservationPendingPaymentEvent;
import payment.order.Order;
import payment.order.OrderService;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
@Transactional
@RequiredArgsConstructor
public class ReservationCommandService {

    private final WaitingCommandService waitingCommandService;
    private final OrderService orderService;
    private final ApplicationEventPublisher eventPublisher;

    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final Clock clock;

    public Reservation create(String name, LocalDate date, long timeId, long themeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(name);
        Slot slot = new Slot(date, findTimeReference(timeId), findThemeReference(themeId));
        Reservation reservation = Reservation.forNew(member, slot);

        validateCreatable(now, slot);

        return save(reservation);
    }

    public PendingReservation createPendingPaymentReservation(String name, LocalDate date, long timeId, long themeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(name);
        Theme theme = findThemeReference(themeId);
        Slot slot = new Slot(date, findTimeReference(timeId), theme);
        Reservation reservation = Reservation.forPendingPayment(member, slot);

        slot.validateNotPast(now);
        Optional<PendingReservation> existingPending = findReusablePendingReservation(slot, member);
        if (existingPending.isPresent()) {
            return existingPending.get();
        }
        validateNoPendingPaymentForMember(member);

        Reservation savedReservation = save(reservation);
        eventPublisher.publishEvent(new ReservationPendingPaymentEvent(savedReservation.id(), now));
        Order order = orderService.findByReservationId(savedReservation.id())
                .orElseThrow(() -> new ResourceNotFoundException("요청한 결제 주문을 찾을 수 없습니다."));

        return PendingReservation.of(savedReservation, order);
    }

    private Optional<PendingReservation> findReusablePendingReservation(Slot slot, Member member) {
        return reservationDao.findBySlotForUpdate(slot)
                .map(existing -> pendingReservationFrom(existing, member));
    }

    private PendingReservation pendingReservationFrom(Reservation reservation, Member member) {
        if (reservation.status() != ReservationStatus.PENDING_PAYMENT) {
            throw duplicateReservationException();
        }
        if (!reservation.isOwnedBy(member)) {
            throw pendingPaymentInProgressException();
        }

        Order order = orderService.findByReservationId(reservation.id())
                .orElseThrow(() -> new ResourceNotFoundException("요청한 결제 주문을 찾을 수 없습니다."));
        return PendingReservation.of(reservation, order);
    }

    public PendingReservation getPendingPaymentReservation(long reservationId, String name) {
        Member member = new Member(name);
        Reservation reservation = findReservation(reservationId);

        reservation.validateOwnedBy(member);
        if (reservation.status() != ReservationStatus.PENDING_PAYMENT) {
            throw new ResourceNotFoundException("결제 대기 예약을 찾을 수 없습니다.");
        }

        Order order = orderService.findByReservationId(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("요청한 결제 주문을 찾을 수 없습니다."));

        return PendingReservation.of(reservation, order);
    }

    private Reservation save(Reservation reservation) {
        try {
            return reservationDao.save(reservation);
        } catch (DataIntegrityViolationException e) {
            throw duplicateReservationException();
        }
    }

    private void validateCreatable(LocalDateTime now, Slot slot) {
        slot.validateNotPast(now);
        validateNoDuplicate(slot);
    }

    public void delete(long reservationId) {
        Reservation reservation = findReservation(reservationId);

        reservationDao.deleteById(reservationId);
        waitingCommandService.promoteNextWaitingIn(reservation.slot());
    }

    public void cancel(long reservationId, String name) {
        Member member = new Member(name);
        Reservation reservation = findReservation(reservationId);

        reservation.validateOwnedBy(member);
        reservation.validateNotStarted(LocalDateTime.now(clock));

        reservationDao.deleteById(reservationId);
        waitingCommandService.promoteNextWaitingIn(reservation.slot());
    }

    public Reservation update(long reservationId, String name, LocalDate newDate, long newTimeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(name);
        Reservation oldReservation = findReservation(reservationId);
        Slot newSlot = new Slot(newDate, findTimeReference(newTimeId), oldReservation.slot().theme());

        oldReservation.validateOwnedBy(member);
        oldReservation.validateNotStarted(now);
        newSlot.validateNotPast(now);
        validateNoDuplicateExcluding(newSlot, reservationId);

        Reservation updated;
        try {
            updated = reservationDao.update(oldReservation.withSlot(newSlot));
        } catch (DataIntegrityViolationException e) {
            throw duplicateReservationException();
        }

        if (!oldReservation.slot().isSameSlot(newSlot)) {
            waitingCommandService.promoteNextWaitingIn(oldReservation.slot());
        }
        return updated;
    }

    private void validateNoDuplicate(Slot slot) {
        if (reservationDao.existsBySlot(slot)) {
            throw duplicateReservationException();
        }
    }

    private void validateNoDuplicateExcluding(Slot slot, long excludedId) {
        reservationDao.findBySlot(slot)
                .filter(existing -> existing.id() != excludedId)
                .ifPresent(existing -> {
                    throw duplicateReservationException();
                });
    }

    private void validateNoPendingPaymentForMember(Member member) {
        if (reservationDao.existsPendingPaymentByOwner(member)) {
            throw new DuplicateException("이미 결제 대기 중인 예약이 있습니다. 결제를 완료하거나 취소 후 다시 시도해주세요.");
        }
    }

    private DuplicateException duplicateReservationException() {
        return new DuplicateException("해당 시간에 이미 예약이 존재합니다.");
    }

    private DuplicateException pendingPaymentInProgressException() {
        return new DuplicateException("해당 시간은 현재 결제 진행 중입니다. 잠시 후 다시 시도해주세요.");
    }

    private ReservationTime findTimeReference(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeReference(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }

    private Reservation findReservation(long reservationId) {
        return reservationDao.findById(reservationId)
                .orElseThrow(() -> new ResourceNotFoundException("요청한 예약을 찾을 수 없습니다."));
    }

}
