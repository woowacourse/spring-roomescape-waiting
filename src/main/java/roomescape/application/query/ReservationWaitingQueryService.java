package roomescape.application.query;

import java.util.List;
import java.util.Optional;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.Member;
import roomescape.domain.ReservationWaiting;
import roomescape.domain.ReservationWaitingQueryRepository;
import roomescape.domain.ReservationWaitingRepository;
import roomescape.domain.Slot;
import roomescape.domain.exception.NotFoundException;
import roomescape.domain.projection.ReservationWaitingWithOrder;

@Service
@Transactional(readOnly = true)
public class ReservationWaitingQueryService {

    private final ReservationWaitingRepository reservationWaitingRepository;
    private final ReservationWaitingQueryRepository reservationWaitingQueryRepository;

    public ReservationWaitingQueryService(
            ReservationWaitingRepository reservationWaitingRepository,
            ReservationWaitingQueryRepository reservationWaitingQueryRepository
    ) {
        this.reservationWaitingRepository = reservationWaitingRepository;
        this.reservationWaitingQueryRepository = reservationWaitingQueryRepository;
    }

    public ReservationWaiting getById(Long id) {
        return reservationWaitingRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약대기입니다. Id: " + id));
    }

    public Optional<ReservationWaiting> findFirstBySlot(Slot slot) {
        return reservationWaitingRepository.findFirstBySlot(slot);
    }

    public ReservationWaitingWithOrder getWithOrderById(Long id) {
        return reservationWaitingQueryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("존재하지않는 예약대기입니다. Id: " + id));
    }

    public List<ReservationWaitingWithOrder> findMine(Member member) {
        return reservationWaitingQueryRepository.findByMember(member);
    }
}
