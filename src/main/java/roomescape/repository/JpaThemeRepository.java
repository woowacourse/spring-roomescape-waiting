package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT th.*
            FROM theme th
            LEFT JOIN reservation r ON r.theme_id = th.id AND r.date >= ? AND r.date < ?
            GROUP BY th.id
            ORDER BY COUNT(r.id) DESC
            """, nativeQuery = true)
    List<Theme> findMostReservedThemesBetweenDate(LocalDate start, LocalDate end);
}
