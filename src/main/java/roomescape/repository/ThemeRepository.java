package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.theme.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query("""
            SELECT s.theme FROM Reservation r
            JOIN r.slot s
            WHERE s.date.value BETWEEN :startDate AND :endDate
            GROUP BY s.theme
            ORDER BY COUNT(s.theme) DESC, s.theme.id DESC
            """)
    List<Theme> findFamous(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
