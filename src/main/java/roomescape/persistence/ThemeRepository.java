package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query(value = """
            SELECT t
            FROM Theme t
            INNER JOIN Reservation r ON t.id = r.theme.id
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC, t.name ASC
            LIMIT :limit
            """)
    List<Theme> findRankByDate(@Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("limit") int limit);
}
