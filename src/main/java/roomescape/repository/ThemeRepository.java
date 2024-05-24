package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.reservation.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query("""
            SELECT r.schedule.theme
            FROM Reservation r
            WHERE r.schedule.date BETWEEN :start AND :end
            GROUP BY r.schedule.theme
            ORDER BY COUNT(r) DESC
            LIMIT :limit
            """)
    List<Theme> findTopReservedThemesByDateRangeAndLimit(LocalDate start, LocalDate end, int limit);

}
