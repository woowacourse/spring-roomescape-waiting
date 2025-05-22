package roomescape.reservation.domain;

import java.util.List;

public interface WaitingRepository {

    boolean exists(Long reservationId, Long memberId);

    Waiting save(Waiting withoutId);

    List<WaitingWithRank> findByMemberId(Long memberId);
}
