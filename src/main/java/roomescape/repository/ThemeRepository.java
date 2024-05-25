package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query("""
            SELECT th
            FROM Theme th
            JOIN Reservation r ON th.id = r.theme.id
            WHERE r.date >= :start AND r.date <= :end
            GROUP BY th.id, th.name, th.description, th.thumbnail
            ORDER BY COUNT(r) DESC, th.id
            """)
    List<Theme> findAndOrderByPopularityFirstTheme(LocalDate start, LocalDate end, Pageable pageable);
}
