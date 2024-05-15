package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {


    @Query("""
            SELECT th FROM Theme AS th
            JOIN Reservation AS r ON th.id = r.theme.id
            WHERE r.date.value BETWEEN :start AND :end
            GROUP BY th.id
            ORDER BY count (r.id) DESC
            """)
    List<Theme> findTopByDurationAndCount(LocalDate start, LocalDate end, Pageable pageable);
}
