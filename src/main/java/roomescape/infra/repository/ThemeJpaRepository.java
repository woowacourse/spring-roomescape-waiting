package roomescape.infra.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import roomescape.domain.reservationdetail.Theme;
import roomescape.domain.reservationdetail.ThemeRepository;

public interface ThemeJpaRepository extends
        ThemeRepository,
        Repository<Theme, Long> {

    @Override
    Theme save(Theme theme);

    @Override
    Optional<Theme> findById(Long id);

    @Override
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

    @Override
    List<Theme> findAll();

    @Override
    void deleteById(Long themeId);
}
