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
                rt.startAt,
                CASE
                    WHEN COUNT(rt.id) = 1 THEN TRUE
                    ELSE FALSE
                END
            )
            FROM ReservationTime AS rt
            LEFT JOIN Reservation AS r ON r.reservationTime.id = rt.id
            GROUP BY rt.id
            ORDER BY rt.startAt
            """)
    List<ReservationTimeWithBookState> findReservationTimesWithBookState(Theme theme, LocalDate date);

    boolean existsByStartAt(LocalTime startAt);
}
