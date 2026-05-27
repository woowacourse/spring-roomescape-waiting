package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    boolean existsByNameAndDateAndTimeAndTheme(String name, LocalDate date, ReservationTime time, Theme theme);

    Optional<Long> findMaxWaitingNumberBy(LocalDate date, ReservationTime reservationTime, Theme theme);

    List<Waiting> findByName(String name);

    void delete(Long id);
}
