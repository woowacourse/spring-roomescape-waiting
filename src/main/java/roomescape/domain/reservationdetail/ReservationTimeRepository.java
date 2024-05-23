package roomescape.domain.reservationdetail;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.exception.time.NotFoundReservationTimeException;

public interface ReservationTimeRepository {
    ReservationTime save(ReservationTime time);

    default ReservationTime getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundReservationTimeException::new);
    }

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    List<ReservationTime> findAllReservedTimeByDateAndThemeId(LocalDate date, Long themeId);

    boolean existsByStartAt(LocalTime startAt);

    void delete(ReservationTime time);
}
