package roomescape.waiting.domain.repository;

import java.util.List;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    List<Waiting> findByWaitingMemberId(Long id);

    void deleteByReservationId(Long reservationId, Long memberId);

    boolean existsByReservationIdAndMemberId(Long reservationId, Long memberId);
}
