package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query(value = """
            select theme.id, theme.name, theme.description, theme.thumbnail
                         from theme
                         left join reservation on reservation.theme_id = theme.id
                         and reservation.date between :fromDate and :toDate
                         group by theme.id
                         order by count(theme_id) desc limit :count
            """, nativeQuery = true)
    List<Theme> findPopularThemeByDate(final LocalDate fromDate, final LocalDate toDate, final Long count);
}
