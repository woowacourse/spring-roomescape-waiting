package roomescape.domain.reservationtime;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    Optional<ReservationTime> findReservationTimeById(long id);

    List<ReservationTime> findAllReservationTime();

    List<AvailableReservationTime> findAvailableReservationTime(LocalDate date, Long themeId);

    Long insert(ReservationTime reservationTime);

    void save(Long id, LocalTime startAt);

    void delete(Long id);
}
