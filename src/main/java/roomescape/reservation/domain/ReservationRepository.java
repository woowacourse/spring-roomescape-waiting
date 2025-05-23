package roomescape.reservation.domain;

import roomescape.theme.domain.Theme;
import roomescape.theme.domain.ThemeId;
import roomescape.timeslot.domain.TimeSlotId;
import roomescape.timeslot.domain.ReservationTime;
import roomescape.user.domain.UserId;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface ReservationRepository {

    boolean existsByParams(ReservationId id);

    boolean existsByParams(ReservationDate date, ReservationTime time, ThemeId themeId);

    Optional<Reservation> findById(ReservationId id);

    List<ReservationTime> findTimeValuesByParams(ReservationDate date, ThemeId themeId);

    List<TimeSlotId> findTimeIdByParams(ReservationDate date, ThemeId themeId);

    List<Reservation> findAll();

    List<Reservation> findAllByUserId(UserId userId);

    Map<Theme, Integer> findThemesToBookedCountByParamsOrderByBookedCount(ReservationDate startDate, ReservationDate endDate, int count);

    List<Reservation> findAllByParams(UserId userId, ThemeId themeId, ReservationDate reservationDate, ReservationDate reservationDate1);

    Reservation save(Reservation reservation);

    void deleteById(ReservationId id);

    void delete(Reservation target);
}
