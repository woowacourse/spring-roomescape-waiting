package roomescape.core.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.core.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(
            value = "SELECT t FROM Theme t JOIN Reservation r ON t.id = r.theme.id WHERE r.date BETWEEN ?1 AND ?2 GROUP BY t.id ORDER BY count(r.id) DESC LIMIT 10",
            countQuery = "SELECT count(r) FROM Reservation r"
    )
    List<Theme> findPopularThemeBetween(final LocalDate lastWeek, final LocalDate today);

    Integer countByName(final String name);

    void deleteById(final long id);
}
