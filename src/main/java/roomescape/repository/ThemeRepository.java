package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {
    // TODO: 쿼리문 사용 방법 확인
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
