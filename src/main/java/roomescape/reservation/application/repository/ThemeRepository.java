package roomescape.reservation.application.repository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Theme;

@Repository
public interface ThemeRepository extends CrudRepository<Theme, Long> {
    List<Theme> findAll();

    @Query("""
                        SELECT t.id, t.name, t.description, t.thumbnail
                        FROM Theme t
                        INNER JOIN Reservation r ON t.id = r.theme.id
                        WHERE r.date BETWEEN TIMESTAMPADD(DAY, -7, CURRENT_DATE()) AND TIMESTAMPADD(DAY, -1, CURRENT_DATE())
                        GROUP BY t.id, t.name, t.description, t.thumbnail
                        ORDER BY COUNT(*) DESC
                        LIMIT 10
            """)
    List<Theme> findPopularThemes();
}