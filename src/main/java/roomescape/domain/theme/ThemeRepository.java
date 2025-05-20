package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme t
            JOIN Reservation r ON r.theme.id = t.id
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY t
            ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findRankingByPeriod(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
