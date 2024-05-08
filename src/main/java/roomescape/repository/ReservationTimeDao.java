package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import roomescape.model.ReservationTime;

public interface ReservationTimeDao {
    List<ReservationTime> findAllReservationTimes();

    List<ReservationTime> findAllReservedTimes(LocalDate date, long themeId);

    ReservationTime findReservationById(long id);

    ReservationTime addReservationTime(ReservationTime reservationTime);

    void deleteReservationTime(long id);

    long countReservationTimeById(long id);

    long countReservationTimeByStartAt(LocalTime startAt);
}
