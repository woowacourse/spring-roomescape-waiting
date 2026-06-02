package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    void delete(Long id);

    Optional<Waiting> findById(Long id);

    List<Waiting> findAll();

    List<Waiting> findByName(String name);

    Optional<Long> findMaxWaitingNumberBy(LocalDate date, ReservationTime reservationTime,
            Theme theme);

    boolean existsByNameAndDateAndTimeAndTheme(String name, LocalDate date, ReservationTime time,
            Theme theme);

}
