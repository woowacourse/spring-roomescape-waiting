package roomescape.reservation.waiting.application.service;

import java.util.List;
import org.springframework.stereotype.Service;
import roomescape.reservation.waiting.domain.WaitingReservationRepository;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

@Service
public class WaitingReservationQueryService {

    private final WaitingReservationRepository waitingReservationRepository;

    public WaitingReservationQueryService(WaitingReservationRepository waitingReservationRepository) {
        this.waitingReservationRepository = waitingReservationRepository;
    }

    public List<WaitingReservationWithRank> findWaitingsWithRankByMemberId(Long memberId) {
        return waitingReservationRepository.findWaitingsWithRankByMember_Id(memberId);
    }
}
