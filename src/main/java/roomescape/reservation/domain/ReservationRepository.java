package roomescape.reservation.domain;

import roomescape.reservation.ui.dto.ReservationResponse;
import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.time.domain.ReservationTimeId;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsByParams(ReservationId id);

    boolean existsByParams(ReservationTimeId timeId);

    boolean existsByParams(ReservationDate date, ReservationTimeId timeId, ThemeId themeId);

    Optional<Reservation> findById(ReservationId id);

    List<ReservationTimeId> findTimeIdByParams(ReservationDate date, ThemeId themeId);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(UserId userId);

    Reservation save(Reservation reservation);

    void deleteById(ReservationId id);

    Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> findAllByParams(UserId userId, ThemeId themeId, ReservationDate reservationDate, ReservationDate reservationDate1);
}
