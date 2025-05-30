package roomescape.repository.jpa;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.ReservationTheme;

@Repository
public interface ReservationThemeJpaRepository extends JpaRepository<ReservationTheme, Long> {

    boolean existsByName(final String name);

    @Query(value = """
        SELECT rt.* FROM reservation_theme rt
        JOIN reservation r ON rt.id = r.theme_id
        WHERE r.date BETWEEN DATEADD(DAY, -7, CURRENT_DATE) AND DATEADD(DAY, -1, CURRENT_DATE)
        GROUP BY rt.id, rt.name, rt.description, rt.thumbnail
        ORDER BY COUNT(*) DESC
        LIMIT 10""", nativeQuery = true)
    List<ReservationTheme> findWeeklyThemeOrderByCountDesc();
}
