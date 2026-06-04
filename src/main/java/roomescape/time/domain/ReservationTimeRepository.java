package roomescape.time.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.time.repository.dto.AvailableTimeQueryResult;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime reservationTime);

    Optional<ReservationTime> findById(long id);

    boolean existsByStartAt(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    List<AvailableTimeQueryResult> queryAvailableTimes(long themeId, LocalDate date);

    void delete(ReservationTime time);
}
