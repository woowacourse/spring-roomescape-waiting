package roomescape.reservation.infrastructure.db.dao;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.model.entity.ReservationTheme;

public interface ReservationThemeJpaRepository extends JpaRepository<ReservationTheme, Long> {

    @Query(value = """
            SELECT th.id, th.name, th.description, th.thumbnail
                            FROM reservation r
                            INNER JOIN theme th ON r.theme_id = th.id
                            GROUP BY r.theme_id
                            ORDER BY count(r.theme_id) DESC
                            LIMIT :limit
            """, nativeQuery = true)
    List<ReservationTheme> getOrderByThemeBookedCountWithLimit(int limit);
}
