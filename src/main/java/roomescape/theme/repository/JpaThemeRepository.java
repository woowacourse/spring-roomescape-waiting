package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.theme.Theme;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    // Todo : 인기 테마 조회
    @Query(value = """
                SELECT t.id,
                       t.name,
                       t.description,
                       t.thumbnail_url
                FROM reservation r
                INNER JOIN theme t ON r.theme_id = t.id
                WHERE r.date >= :start AND r.date <= :end
                GROUP BY t.id, t.name, t.description, t.thumbnail_url
                ORDER BY COUNT(*) DESC, t.id ASC 
                LIMIT :limit
    """, nativeQuery = true)
    List<Theme> findPopularThemes(@Param("start") LocalDate start,
                                  @Param("end") LocalDate end,
                                  @Param("limit") int limit);
}
