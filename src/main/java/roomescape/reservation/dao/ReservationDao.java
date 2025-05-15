package roomescape.reservation.dao;

import java.time.LocalDate;
import java.util.List;
import roomescape.common.Dao;
import roomescape.reservation.domain.Reservation;

public interface ReservationDao extends Dao<Reservation> {
    List<Reservation> findAllByMemberId(Long memberId);

    List<Reservation> findAllByMemberAndThemeAndDate(Long memberId, Long themeId, LocalDate dateFrom, LocalDate dateTo);

    Boolean existsByDateAndTimeId(LocalDate date, Long timeId);
}
