package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationTime;
import roomescape.domain.Theme;
import roomescape.dto.business.ReservationTimeWithBookState;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
             SELECT new roomescape.dto.business.ReservationTimeWithBookState (
                rt.id,
                rt.start_at,
                EXISTS (
                    SELECT *
                    FROM reservation AS r
                    WHERE r.time_id = rt.id
                    AND r.date = ?
                    AND r.theme_id = ?
                )
             )
             FROM reservation_time AS rt
             ORDER BY rt.id DESC
            """)
    List<ReservationTimeWithBookState> findReservationTimesWithBookState(Theme theme, LocalDate date);

    boolean existsByStartAt(LocalTime startAt);
}
