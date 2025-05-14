package roomescape.dao;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.model.AvailableReservationTime;
import roomescape.model.ReservationTime;

public interface ReservationTimeDao {
    List<ReservationTime> findAll();

    Long saveTime(ReservationTime reservationTime);

    void deleteTimeById(Long id);

    Optional<ReservationTime> findById(final Long id);

    boolean isDuplicatedStartAtExisted(LocalTime startAt);

    List<AvailableReservationTime> findAvailableTimes(String date, Long themeId);
}
