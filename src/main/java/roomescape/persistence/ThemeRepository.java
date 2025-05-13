package roomescape.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query(value = """
                SELECT t.id, t.name, t.description, t.thumbnail
                FROM theme t
                INNER JOIN reservation r ON t.id = r.theme_id
                WHERE r.date BETWEEN :startDate AND :endDate
                GROUP BY t.id
                ORDER BY COUNT(r.id) DESC, t.name ASC
                LIMIT :limit
            """, nativeQuery = true)
    List<Theme> findRankByDate(@Param("startDate") LocalDate startDate,
                     @Param("endDate") LocalDate endDate,
                     @Param("limit") int limit);
}
