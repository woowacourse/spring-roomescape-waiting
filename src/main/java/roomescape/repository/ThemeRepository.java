package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT t
            FROM Theme t
            JOIN Reservation r ON t.id = r.theme.id
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC, t.id
            LIMIT 10
            """)
    List<Theme> findThemesWithReservationsBetweenDates(LocalDate startDate, LocalDate endDate);
}
