package roomescape.reservation.repository;

import java.util.List;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

public interface ReservationWaitRepository {

    ReservationWait save(ReservationWait reservationWait);

    boolean existsByInfoDateAndInfoTimeIdAndInfoThemeIdAndInfoMemberId(ReservationDate date, Long timeId, Long themeId,
                                                                       Long memberId);

    List<ReservationWaitWithRankResponse> findWithRankByInfoMemberId(Long memberId);

}
