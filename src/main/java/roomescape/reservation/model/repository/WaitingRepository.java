package roomescape.reservation.model.repository;

import java.util.List;
import roomescape.reservation.model.entity.Waiting;
import roomescape.reservation.model.vo.WaitingWithRank;

public interface WaitingRepository {

    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    Waiting save(Waiting waiting);
}
