package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservationitem.ReservationTheme;

@Repository
public interface ReservationThemeJpaRepository extends JpaRepository<ReservationTheme, Long> {

    boolean existsByName(final String name);

    @Query(value = """
        SELECT id, name, description, thumbnail
        FROM (
            SELECT t.id, t.name, t.description, t.thumbnail,
                   RANK() OVER (ORDER BY COUNT(r.id) DESC, t.id ASC) as theme_rank
            FROM reservation_theme t
            LEFT JOIN reservation_item ri ON t.id = ri.theme_id
            LEFT JOIN reservation r ON ri.id = r.reservation_item_id
            WHERE ri.date BETWEEN :startAt AND :endAt
            GROUP BY t.id, t.name, t.description, t.thumbnail
        ) ranked
        WHERE theme_rank <= :rank
        """, nativeQuery = true)
    List<ReservationTheme> findPopularThemesByRankAndDuration(int rank, LocalDate startAt, LocalDate endAt);
}
