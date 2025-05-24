package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

public interface WaitingRepository {

    Waiting save(Waiting waiting);

    Optional<Waiting> findById(Long id);

    List<Waiting> findByMemberId(Long id);

    void deleteById(Long id);

    Optional<Waiting> findByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);
}
