package roomescape.domain.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.Theme;
import roomescape.exception.theme.NotFoundThemeException;

public interface ThemeRepository extends Repository<Theme, Long> {
    Theme save(Theme theme);

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundThemeException::new);
    }

    Optional<Theme> findById(Long id);

    List<Theme> findAll();

    @Query(value = """
            SELECT theme.id, theme.name, theme.description, theme.thumbnail
            FROM reservation_detail
            LEFT JOIN theme ON theme.id=reservation_detail.theme_id
            WHERE reservation_detail.date >= ? AND reservation_detail.date <= ?
            GROUP BY theme.id
            ORDER BY COUNT(*) DESC
            LIMIT ?;
            """, nativeQuery = true)
    List<Theme> findThemesByPeriodWithLimit(String startDate, String endDate, int limit);

    void delete(Theme theme);

    void deleteAll();
}
