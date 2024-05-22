package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.model.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
           SELECT
              th.id, th.name, th.description, th.thumbnail
              FROM theme AS th
              INNER JOIN reservation AS r
              ON r.theme_id = th.id
              WHERE r.date BETWEEN :startAt AND :endAt
              GROUP BY r.theme_id
              ORDER BY COUNT(r.theme_id) DESC
              LIMIT :maximumThemeCount
""", nativeQuery = true)
    List<Theme> findPopularThemes(
            @Param("startAt") LocalDate startAt,
            @Param("endAt") LocalDate endAt,
            @Param("maximumThemeCount") int maximumThemeCount
    );
}
