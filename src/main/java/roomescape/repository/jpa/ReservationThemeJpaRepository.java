package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;

@Repository
public interface ReservationThemeJpaRepository extends JpaRepository<ReservationTheme, Long> {

    boolean existsByName(final String name);

    @Query(value = """
            SELECT t.id, t.name, t.description, t.thumbnail
                        FROM (
                            SELECT t.id, t.name, t.description, t.thumbnail,
                                   RANK() OVER (ORDER BY COUNT(r.THEME_ID) DESC, r.THEME_ID ASC) as theme_rank
                            FROM THEME t
                            LEFT JOIN RESERVATION r ON t.ID = r.THEME_ID
                            WHERE r.date BETWEEN :startAt AND :endAt
                            GROUP BY t.id, t.name, t.description, t.thumbnail
                        ) ranked
                        WHERE theme_rank <= :rank
            """, nativeQuery = true)
    List<ReservationTheme> findPopularThemesByRankAndDuration(int rank, LocalDate startAt, LocalDate endAt);
}
