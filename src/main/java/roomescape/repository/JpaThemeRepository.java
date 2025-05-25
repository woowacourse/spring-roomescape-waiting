package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT th.*
            FROM theme th
            LEFT JOIN reservation r ON r.theme_id = th.id AND r.date >= :from AND r.date <= :to
            GROUP BY th.id
            ORDER BY COUNT(r.id) DESC
            """, nativeQuery = true)
    List<Theme> findMostReservedThemesBetweenDate(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to);
}
