package roomescape.theme.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
            SELECT t.*
                FROM theme t
                RIGHT JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN :startDate AND :endDate
                GROUP BY r.theme_id
                ORDER BY COUNT(r.theme_id) DESC, t.id ASC
                LIMIT :limit
            """, nativeQuery = true)
    List<Theme> findTopNThemeBetweenStartDateAndEndDate(LocalDate startDate, LocalDate endDate, int limit);
}
