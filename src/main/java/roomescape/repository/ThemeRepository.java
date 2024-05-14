package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(nativeQuery = true, value = """
            SELECT t.id, t.name, t.description, t.thumbnail, COUNT(*) as reservation_count
            FROM reservation as r
            INNER JOIN theme as t on r.theme_id = t.id
            WHERE r.date BETWEEN ? AND ?
            GROUP BY t.id
            ORDER BY reservation_count desc
            LIMIT(?)
            """)
    List<Theme> findTopReservedThemesByDateRangeAndLimit(LocalDate start, LocalDate end, int limit);

}
