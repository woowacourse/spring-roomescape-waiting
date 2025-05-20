package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.entity.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM
              Reservation r
              JOIN r.theme t
            WHERE
              r.date between :startDate AND :endDate
            GROUP BY t
            ORDER BY COUNT(t) DESC
        """)
    List<Theme> findTopRankByDateBetween(LocalDate startDate, LocalDate endDate);

    boolean existsByName(String name);
}
