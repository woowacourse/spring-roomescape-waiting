package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

public interface ThemeJpaRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    @Query(value = """
                SELECT t.*
                FROM theme AS t
                INNER JOIN reservation AS r ON r.theme_id = t.id
                WHERE r.date BETWEEN :from AND :to
                GROUP BY t.id, t.name, t.description, t.thumbnail
                ORDER BY COUNT(r.id) DESC
                LIMIT :limit
            """, nativeQuery = true)
    List<Theme> findRankedByPeriod(LocalDate from, LocalDate to, int limit);
}
