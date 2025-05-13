package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.domain.time.AvailableReservationTime;
import roomescape.domain.time.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(final LocalTime time);

    List<ReservationTime> findAll();

    void deleteById(final Long id);

    Optional<ReservationTime> findById(final Long id);

    boolean existByStartAt(final LocalTime startAt);

    List<AvailableReservationTime> findAllAvailableReservationTimes(final LocalDate date, final Long themeId);
}
