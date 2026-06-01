package roomescape.repository;

import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

import java.time.LocalDate;
import java.util.Optional;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findByScheduleAndName(Waiting waiting);

    Optional<Waiting> findById(long id);

    Long countByThemeIdAndDateAndTimeIdAndIdLessThan(Long id, Theme theme, LocalDate date, ReservationTime time);

    void delete(Waiting waiting);
}
