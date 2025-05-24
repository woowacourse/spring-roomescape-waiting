package roomescape.reservation.domain;

import java.util.List;
import roomescape.reservation.domain.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository {

    WaitingReservation save(WaitingReservation waitingReservation);

    List<WaitingReservationWithRank> findWaitingsWithRankByMember_Id(Long memberId);

    void deleteByIdAndMemberId(Long id, Long memberId);

    boolean existsByIdAndMemberId(Long id, Long memberId);
}
