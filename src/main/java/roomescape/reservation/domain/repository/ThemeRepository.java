package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT theme.id, theme.name, theme.description, theme.thumbnail, COUNT(*) AS reservation_count 
            FROM theme 
            INNER JOIN reservation_slot AS rs ON rs.theme_id = theme.id 
            INNER JOIN reservation AS r ON r.reservation_slot_id = rs.id 
            WHERE rs.date BETWEEN ? AND ? 
            GROUP BY theme.id, theme.name 
            ORDER BY reservation_count DESC 
            LIMIT ?;
            """, nativeQuery = true)
    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);
}
