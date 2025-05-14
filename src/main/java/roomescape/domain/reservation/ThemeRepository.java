package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
                SELECT t
                FROM Theme t
                LEFT JOIN Reservation r
                    ON t.id = r.theme.id AND r.date BETWEEN :startDate AND :endDate
                GROUP BY t.id
                ORDER BY COUNT(r.id) DESC
                LIMIT :limit
            """)
    List<Theme> findRankBetweenDate(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    @Param("limit") int limit);
}
