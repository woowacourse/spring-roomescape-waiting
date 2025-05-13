package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.entity.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    List<Theme> findAll();

    // TODO: 쿼리 변경
    @Query(value = """
            SELECT
                t.id,
                t.name,
                t.description,
                t.thumbnail
            FROM theme t
            LEFT JOIN (
                SELECT
                    theme_id,
                    COUNT(*) as cnt
                FROM reservation
                WHERE date BETWEEN :startDate AND :endDate
                GROUP BY theme_id
            ) r_stats ON t.id = r_stats.theme_id
            ORDER BY cnt DESC, theme_id DESC
            FETCH FIRST :limit ROWS ONLY
            """, nativeQuery = true)
    List<Theme> findPopularDescendingUpTo(LocalDate startDate, LocalDate endDate, int limit);

    Optional<Theme> findByName(String name);
}
