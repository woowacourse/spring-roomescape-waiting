package roomescape.repository.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;

@Repository
public interface ReservationThemeJpaRepository extends JpaRepository<ReservationTheme, Long> {

    boolean existsByName(final String name);

    @Query("""
        SELECT th.id, th.name, th.description, th.thumbnail, COUNT(*) AS reservation_count
        FROM Reservation r
        JOIN r.theme th
        WHERE CAST(r.date AS date) BETWEEN 
              FUNCTION('DATE_SUB', CURRENT_DATE, 7) AND 
              FUNCTION('DATE_SUB', CURRENT_DATE, 1)
        GROUP BY th.id, th.name, th.description, th.thumbnail
        ORDER BY reservation_count DESC
        LIMIT 10""")
    List<ReservationTheme> findWeeklyThemeOrderByCountDesc();
}
