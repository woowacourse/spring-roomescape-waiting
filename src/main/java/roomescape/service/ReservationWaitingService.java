package roomescape.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.WaitingWithOrder;
import roomescape.exception.BusinessRuleViolationException;
import roomescape.exception.ConflictException;
import roomescape.exception.NotFoundException;
import roomescape.exception.UnauthorizedException;
import roomescape.repository.ReservationWaitingRepository;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingService {

    private static final String NOT_OWNER = "본인의 예약 대기가 아닙니다.";
    private static final String ALREADY_BOOKED = "이미 대기 중인 예약입니다.";
    private static final String PAST_WAITING_CANCEL_REJECTED = "지난 예약 대기는 취소할 수 없습니다.";

    private final ReservationWaitingRepository reservationWaitingRepository;

    public ReservationWaitingService(ReservationWaitingRepository reservationWaitingRepository) {
        this.reservationWaitingRepository = reservationWaitingRepository;
    }

    @Transactional
    public WaitingWithOrder addWaiting(ReservationWaiting reservationWaiting) {
        if (reservationWaitingRepository.existBy(reservationWaiting.getName(), reservationWaiting.getReservationId())) {
            throw new ConflictException(ALREADY_BOOKED);
        }
        return reservationWaitingRepository.save(reservationWaiting);
    }

    public boolean existBy(String name, Long reservationId) {
        return reservationWaitingRepository.existBy(name, reservationId);
    }

    @Transactional
    public void cancelMyReservationWaiting(Long id, String name) {
        ReservationWaiting reservationWaiting = findById(id);
        if (!reservationWaiting.isOwnedBy(name)) {
            throw new UnauthorizedException(NOT_OWNER);
        }
        if (reservationWaiting.isPast(LocalDateTime.now())) {
            throw new BusinessRuleViolationException(PAST_WAITING_CANCEL_REJECTED);
        }

        reservationWaitingRepository.deleteById(id);
    }

    public ReservationWaiting findById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> NotFoundException.reservationWaiting(id));
    }

    public Optional<ReservationWaiting> findEarliestByReservationId(Long reservationId) {
        return reservationWaitingRepository.findEarliestByReservationId(reservationId);
    }

    @Transactional
    public void deleteById(Long id) {
        reservationWaitingRepository.deleteById(id);
    }

    public List<WaitingWithOrder> getMyReservationWaitings(String name) {
        return reservationWaitingRepository.findByName(name);
    }
}
