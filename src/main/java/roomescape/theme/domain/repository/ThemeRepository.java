package roomescape.theme.domain.repository;

import java.time.LocalDate;
import java.util.Collection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(
            value = """
                    SELECT t.id, t.name, t.description, t.thumbnail 
                    FROM theme t 
                    INNER JOIN reservation r ON r.theme_id = t.id 
                    WHERE r.date BETWEEN :startDate AND :endDate 
                    GROUP BY t.id, t.name, t.description, t.thumbnail 
                    ORDER BY COUNT(r.id) DESC 
                    LIMIT :count
                    """,
            nativeQuery = true
    )
    Collection<Theme> findRankedByPeriod(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("count") int count
    );

    boolean existsByName(String name);
}
