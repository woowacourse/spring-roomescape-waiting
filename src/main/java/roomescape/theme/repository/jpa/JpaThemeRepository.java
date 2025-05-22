package roomescape.theme.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
                SELECT t
                FROM Theme t
                WHERE t.id IN (
                    SELECT r.theme.id
                    FROM Reservation r
                    WHERE r.date BETWEEN :fromDate AND :toDate
                    GROUP BY r.theme.id
                    ORDER BY COUNT(r) DESC
                    LIMIT :listNum
                )
            """)
    List<Theme> findTopByReservationCountDesc(LocalDate fromDate, LocalDate toDate, long listNum);
}
