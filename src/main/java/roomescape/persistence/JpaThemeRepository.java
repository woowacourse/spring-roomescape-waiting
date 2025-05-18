package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
                    SELECT t.id, t.name, t.description, t.thumbnail
                    FROM Theme t
                    INNER JOIN Reservation r ON t.id = r.theme_id
                    WHERE r.date BETWEEN :startDate AND :endDate
                    GROUP BY t.id, t.name, t.description, t.thumbnail
                    ORDER BY COUNT(r.id) DESC, t.name ASC
                    LIMIT :limit
                """, nativeQuery = true)
    List<Theme> findRankByDate(LocalDate startDate, LocalDate endDate, int limit);
}
