package roomescape.theme.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationDate;
import roomescape.theme.domain.Theme;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
                    SELECT t FROM Theme t
                    LEFT JOIN Reservation r ON r.theme = t
                    WHERE r.date BETWEEN :startDate AND :endDate
                    GROUP BY t
                    ORDER BY COUNT(r) DESC
            """)
    List<Theme> findPopularThemes(
            @Param("startDate") ReservationDate startDate,
            @Param("endDate") ReservationDate endDate,
            Pageable pageable
    );
}
