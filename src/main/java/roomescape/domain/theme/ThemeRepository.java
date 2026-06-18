package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
        select t from Theme t
        join Reservation r on t = r.theme
        where r.date.playDay between :startDate and :endDate
        group by t
        order by count(r) desc, t.id asc
        limit :rankLimit
    """)
    List<Theme> findPopularThemes(int rankLimit, LocalDate startDate, LocalDate endDate);
}
