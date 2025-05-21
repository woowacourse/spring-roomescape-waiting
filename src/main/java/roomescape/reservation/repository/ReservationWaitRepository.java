package roomescape.reservation.repository;

import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.ReservationWait;

public interface ReservationWaitRepository {

    ReservationWait save(ReservationWait reservationWait);

    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(ReservationDate date, Long timeId, Long themeId, Long memberId);
}
