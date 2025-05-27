package roomescape.waiting.repository.jpa;

import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;
import roomescape.waiting.domain.Waiting;
import roomescape.waiting.repository.WaitingRepository;

public interface WaitingJpaRepository extends JpaRepository<Waiting, Long>, WaitingRepository {

    @Override
    @Query("""
            SELECT w 
            from Waiting w 
            WHERE w.date = :date AND w.time = :time AND w.theme = :theme
            ORDER BY w.priority ASC 
            LIMIT 1
            """
    )
    Optional<Waiting> popFirstWaiting(
            @Param(value = "theme") Theme theme,
            @Param(value = "date") LocalDate date,
            @Param(value = "time") ReservationTime time
    );
}
