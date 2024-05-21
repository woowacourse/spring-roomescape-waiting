package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    boolean existsByStartAt(LocalTime time);

    List<TimeSlot> getReservationTimeAvailabilities(LocalDate date, long themeId);

    ReservationTime getById(long id);

    void deleteById(long id);
}
