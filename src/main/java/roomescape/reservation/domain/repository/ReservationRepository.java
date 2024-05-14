package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.domain.Theme;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    @Query(value = """
                SELECT 1
                FROM reservation as r 
                INNER JOIN reservation_time as t ON r.time_id = t.id 
                INNER JOIN theme as th ON r.theme_id = th.id 
                WHERE date = ? AND time_id = ? AND theme_id = ?
                LIMIT 1;
    """, nativeQuery = true)
    int existsBy(LocalDate date, ReservationTime time, Theme theme);
}
