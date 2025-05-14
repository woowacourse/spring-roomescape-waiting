package roomescape.dao;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import roomescape.entity.ReservationTime;

public interface ReservationTimeDao {

    List<ReservationTime> findAllTimes();

    List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId);

    ReservationTime findTimeById(Long id);

    boolean existTimeByStartAt(LocalTime startAt);

    ReservationTime addTime(ReservationTime reservationTime);

    void removeTimeById(Long id);
}
