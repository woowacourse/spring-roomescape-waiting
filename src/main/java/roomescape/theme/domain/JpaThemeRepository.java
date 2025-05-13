package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface JpaThemeRepository extends JpaRepository<Theme,Long> {
    @Query(value = """
        SELECT t.*
        FROM theme t
        LEFT JOIN reservation r ON t.id = r.theme_id
        WHERE r.date BETWEEN :start AND :end
        GROUP BY t.id
        ORDER BY COUNT(r.id) DESC
        LIMIT :limit
        """, nativeQuery = true)
    List<Theme> findPopularThemes(LocalDate start, LocalDate end, int limit);
}
