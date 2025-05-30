package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.theme.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t FROM Theme t
            LEFT JOIN ReservationSchedule rs on t.id = rs.theme.id
            INNER JOIN Reservation r on rs.id = r.schedule.id
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findPopularThemeDuringAWeek(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
