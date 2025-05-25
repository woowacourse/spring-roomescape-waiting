package roomescape.waiting.domain.repository;

import java.util.List;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.domain.WaitingStatus;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    List<Waiting> findByWaitingMemberId(Long id);

    void deleteByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    boolean existsByBookingSlotIdAndMemberId(Long reservationId, Long memberId);

    List<Waiting> findAllByWaitingStatus(WaitingStatus waitingStatus);

    void deleteById(Long waitingId);
}
