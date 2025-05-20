package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends CrudRepository<Theme, Long> {

    @Query("""
        SELECT t 
        FROM Theme t
        JOIN Reservation r ON t.id = r.theme.id
        WHERE r.date BETWEEN :startDate AND :endDate
        GROUP BY t
        ORDER BY COUNT(r) DESC
        """)
    List<Theme> findPopularThemes(@Param("startDate") LocalDate start, @Param("endDate") LocalDate end);

    List<Theme> findAll();
}
