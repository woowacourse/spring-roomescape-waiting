package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme t
            LEFT JOIN Reservation r
                ON t.id = r.theme.id AND r.date BETWEEN :startDate AND :endDate
            GROUP BY t.id
            ORDER BY COUNT(r.id) DESC, t.id ASC
            """)
    List<Theme> findRankBetweenDate(@Param("startDate") LocalDate startDate,
                                    @Param("endDate") LocalDate endDate,
                                    Pageable pageable);

    boolean existsByName(@Param("name") String name);
}
