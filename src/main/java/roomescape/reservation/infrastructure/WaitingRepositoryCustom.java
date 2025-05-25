package roomescape.reservation.infrastructure;

import java.util.List;
import roomescape.reservation.dto.response.WaitingWithRankResponse;

public interface WaitingRepositoryCustom {

    List<WaitingWithRankResponse> findByMemberIdWithRank(Long memberId);
}
