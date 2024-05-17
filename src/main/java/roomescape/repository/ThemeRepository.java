package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;
import roomescape.repository.exception.ThemeNotFoundException;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT th
            FROM Theme th
            LEFT JOIN Reservation r ON th.id = r.theme.id
            WHERE r.date
            BETWEEN :startDate AND :endDate
            GROUP BY th.id
            ORDER BY count(r) desc
            LIMIT :limit
            """)
    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);

    default Theme fetchById(long themeId) {
        return findById(themeId).orElseThrow(ThemeNotFoundException::new);
    }
}
