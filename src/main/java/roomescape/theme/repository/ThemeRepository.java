package roomescape.theme.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(value = """
          SELECT t.* FROM theme t
          JOIN reservation r ON r.theme_id = t.id
          JOIN reservation_time rt ON r.time_id = rt.id
          WHERE rt.start_time >= :start AND rt.start_time < :end
          GROUP BY t.id, t.name, t.description, t.image_url
          ORDER BY COUNT(r.id) DESC, t.id ASC
          LIMIT :limit
          """, nativeQuery = true)
    List<Theme> findBestThemesByDate(LocalDate start, LocalDate end, int limit);
}
