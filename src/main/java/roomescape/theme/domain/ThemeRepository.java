package roomescape.theme.domain;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {


    @Query("""
            SELECT t FROM Theme AS t
            JOIN Reservation AS r
            WHERE r.date.date BETWEEN :start AND :end
            GROUP BY t.id
            ORDER BY count (r.id) DESC
            """)
    List<Theme> findTopByDurationAndCount(LocalDate start, LocalDate end, Pageable pageable);
}
