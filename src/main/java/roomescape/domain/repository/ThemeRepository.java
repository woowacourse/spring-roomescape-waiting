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

    @Query(value = """
            select theme.id, theme.name, theme.description, theme.thumbnail
            from reservation_detail
            left join theme on theme.id=reservation_detail.theme_id
            where reservation_detail.date >= ? and reservation_detail.date <= ?
            group by theme.id
            order by count(*) desc
            limit ?;
            """, nativeQuery = true)
    List<Theme> findThemesByPeriodWithLimit(String startDate, String endDate, int limit);

    List<Theme> findAll();

    void delete(Theme theme);
}
