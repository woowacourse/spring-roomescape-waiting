package roomescape.reservation.repository;

import java.util.List;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;
import roomescape.reservation.service.dto.ReservationWaitWithRankResponse;

public interface ReservationWaitRepository {

    List<ReservationWait> findAll();

    List<ReservationWaitWithRankResponse> findWithRankByInfoMemberId(Long memberId);

    ReservationWait save(ReservationWait reservationWait);

    void deleteById(Long id);

    boolean existsByInfoDateAndInfoTimeIdAndInfoThemeIdAndInfoMemberId(
            ReservationDate date,
            Long timeId,
            Long themeId,
            Long memberId
    );
}
