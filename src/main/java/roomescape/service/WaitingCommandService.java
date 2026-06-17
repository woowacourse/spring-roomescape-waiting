package roomescape.service;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationTime;
import roomescape.domain.Slot;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;
import roomescape.exception.DuplicateException;
import roomescape.exception.InvalidReferenceException;
import roomescape.exception.ResourceNotFoundException;
import roomescape.exception.TemporaryConflictException;
import payment.order.Order;
import payment.order.OrderRepository;
import roomescape.repository.ReservationDao;
import roomescape.repository.ReservationTimeDao;
import roomescape.repository.ThemeDao;
import roomescape.repository.WaitingDao;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@Transactional
@RequiredArgsConstructor
public class WaitingCommandService {
    private static final long RESERVATION_AMOUNT = 5_000L;

    private final WaitingDao waitingDao;
    private final ReservationDao reservationDao;
    private final ReservationTimeDao reservationTimeDao;
    private final ThemeDao themeDao;
    private final OrderRepository orderRepository;
    private final Clock clock;

    public Waiting create(String name, LocalDate date, long timeId, long themeId) {
        LocalDateTime now = LocalDateTime.now(clock);
        Member member = new Member(name);
        Slot slot = new Slot(date, findTimeReference(timeId), findThemeReference(themeId));

        slot.validateNotPast(now);
        Reservation reservation = findConfirmedReservationBySlot(slot);
        validateNotOwnReservation(reservation, member);
        validateNoDuplicateWaiting(slot, member);

        try {
            return waitingDao.save(Waiting.forNew(member, slot, now));
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateException("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없습니다.");
        }
    }

    public void cancel(long waitingId, String name) {
        Member member = new Member(name);
        Waiting waiting = findWaitingReference(waitingId);

        waiting.validateOwnedBy(member);
        waiting.validateNotStarted(LocalDateTime.now(clock));

        waitingDao.deleteById(waitingId);
    }

    private Reservation findConfirmedReservationBySlot(Slot slot) {
        return reservationDao.findConfirmedBySlotForUpdate(slot)
                .orElseThrow(() -> new ResourceNotFoundException("해당 날짜와 시간에 확정된 예약이 존재하지 않습니다."));
    }

    private void validateNotOwnReservation(Reservation reservation, Member member) {
        if (reservation.isOwnedBy(member)) {
            throw new DuplicateException("내가 예약한 시간에 예약대기를 생성할 수 없습니다.");
        }
    }

    private void validateNoDuplicateWaiting(Slot slot, Member member) {
        if (waitingDao.existsBySlotAndOwner(slot, member)) {
            throw new DuplicateException("같은 날짜/시간/테마에 여러 개의 예약 대기를 생성할 수 없습니다.");
        }
    }

    private Waiting findWaitingReference(long waitingId) {
        return waitingDao.findById(waitingId)
                .orElseThrow(() -> new ResourceNotFoundException("존재하지 않는 예약 대기입니다."));
    }

    private ReservationTime findTimeReference(long timeId) {
        return reservationTimeDao.findById(timeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 예약 시간입니다."));
    }

    private Theme findThemeReference(long themeId) {
        return themeDao.findById(themeId)
                .orElseThrow(() -> new InvalidReferenceException("존재하지 않는 테마입니다."));
    }

    public void promoteNextWaitingIn(Slot slot) {
        if (slot.isPast(LocalDateTime.now(clock))) {
            return;
        }
        waitingDao.findNextInLineForUpdate(slot)
                .ifPresent(this::promoteWaiting);
    }

    private void promoteWaiting(Waiting waiting) {
        waitingDao.deleteById(waiting.id());
        try {
            Reservation reservation = reservationDao.save(Reservation.forPendingPayment(waiting.owner(), waiting.slot()));
            orderRepository.save(Order.ready(
                    createOrderId(),
                    reservation.id(),
                    RESERVATION_AMOUNT,
                    LocalDateTime.now(clock)
            ));
        } catch (DataIntegrityViolationException e) {
            throw new TemporaryConflictException("일시적인 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        }
    }

    private String createOrderId() {
        return "order_" + UUID.randomUUID().toString().replace("-", "");
    }
}
