package roomescape.domain.reservationdetail;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime time);

    ReservationTime getById(Long id);

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    List<ReservationTime> findAllReservedTimeByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByStartAt(LocalTime startAt);

    void delete(ReservationTime time);
}
