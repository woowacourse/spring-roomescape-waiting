package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.theme.domain.Theme;
import roomescape.waiting.domain.Waiting;

public interface WaitingRepository {
    Waiting save(Waiting waiting);

    boolean existsByNameAndDateAndTimeAndTheme(String name, LocalDate date, ReservationTime time, Theme theme);

    Optional<Long> findMaxWaitingNumberBy(LocalDate date, ReservationTime reservationTime, Theme theme);

    List<Waiting> findByName(String name);

    Optional<Waiting> findById(Long id);

    void delete(Long id);
}
