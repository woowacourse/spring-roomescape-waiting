package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;

import java.util.List;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
            SELECT th.id, th.name, th.description, th.thumbnail
            FROM theme AS th
            INNER JOIN reservation AS r ON th.id = r.theme_id
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY th.id
            ORDER BY COUNT(th.id) DESC
            LIMIT :themeCount;
            """, nativeQuery = true)
    List<Theme> findTopThemesDescendingByDescription(String startDate, String endDate, int count);
}
