package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(""" 
            SELECT t.id, t.name, t.description, t.thumbnail
                               FROM Theme t
                               JOIN Reservation r
                               WHERE r.date BETWEEN :startDate AND :endDate
                               GROUP BY t.id, t.name, t.description, t.thumbnail
                               ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findRankingByPeriod(LocalDate startDate, LocalDate endDate, int limit);
}
