package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT
                th.id AS theme_id,
                th.name AS theme_name,
                th.description AS theme_description,
                th.thumbnail AS theme_thumbnail
            FROM reservation r, theme th
            WHERE r.theme_id = th.id AND r.`date` BETWEEN ?1 AND ?2
            GROUP BY theme_id
            ORDER BY COUNT(theme_id) DESC, theme_id
            LIMIT 10;
            """, nativeQuery = true)
    List<Theme> findThemesWithReservationsBetweenDates(LocalDate startDate, LocalDate endDate);
}
