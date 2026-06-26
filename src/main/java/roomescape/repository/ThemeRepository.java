package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT t.* FROM session s
            INNER JOIN reservation r ON s.id = r.session_id
            INNER JOIN theme t ON s.theme_id = t.id
            WHERE s.date BETWEEN :fromDate AND :toDate
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC
            LIMIT :topCount
            """, nativeQuery = true)
    List<Theme> findPopularThemes(@Param("topCount") Long topCount,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate);
}
