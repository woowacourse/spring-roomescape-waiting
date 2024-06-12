package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;
import roomescape.domain.theme.ThemeRepository;

@Repository
public interface ThemeJpaRepository extends ThemeRepository, JpaRepository<Theme, Long> {

    @Query(nativeQuery = true, value = """
        SELECT
            th.id AS id,
            th.name AS name,
            th.description AS description,
            th.thumbnail AS thumbnail,
            COUNT(r.theme_id) AS count
        FROM theme AS th
        LEFT JOIN reservation AS r ON th.id = r.theme_id AND r.date BETWEEN :start AND :end
        GROUP BY th.id
        ORDER BY count DESC
        LIMIT :themeCount
        """)
    List<Theme> findPopular(LocalDate start, LocalDate end, int themeCount);
}
