package roomescape.reservation.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT theme.id, theme.name, theme.description, theme.thumbnail, COUNT(*) AS reservation_count 
            FROM theme 
            INNER JOIN reservation AS re ON re.theme_id = theme.id 
            INNER JOIN member_reservation AS mr ON mr.reservation_id = re.id 
            WHERE re.date BETWEEN ? AND ? 
            GROUP BY theme.id, theme.name 
            ORDER BY reservation_count DESC 
            LIMIT ?;
            """, nativeQuery = true)
    List<Theme> findTopThemesByReservations(LocalDate startDate, LocalDate endDate, int limit);
}
