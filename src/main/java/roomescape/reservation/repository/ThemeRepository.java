package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme as t
            LEFT JOIN Reservation r
              ON r.slot.theme = t
            GROUP BY t.id
            ORDER BY count(t) DESC
            """)
    List<Theme> findAllOrderByReservationCount(Pageable pageable);
}
