package roomescape.reservation.waiting.domain;

import java.util.List;
import roomescape.reservation.waiting.domain.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId);

    void deleteByIdAndMemberId(Long id, Long memberId);

    boolean existsByIdAndMemberId(Long id, Long memberId);
}
