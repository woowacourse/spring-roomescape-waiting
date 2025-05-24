package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.domain.Waiting;

@Repository
public interface JpaWaitingRepository extends JpaRepository<Waiting, Long> {

    List<Waiting> findByMemberId(Long id);

    Optional<Waiting> findByDateAndReservationTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);
}
