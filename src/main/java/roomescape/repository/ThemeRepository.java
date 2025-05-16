package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT t.*
            FROM theme t
            INNER JOIN reservation r ON r.theme_id = t.id
            WHERE r.date BETWEEN :from AND :to
            GROUP BY t.id
            ORDER BY COUNT(*) DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<Theme> findThemesOrderByReservationCount(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            @Param("limit") int limit);
}
