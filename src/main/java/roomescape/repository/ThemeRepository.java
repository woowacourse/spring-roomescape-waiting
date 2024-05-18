package roomescape.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query("""
            select t from Theme t
            left join Reservation r on r.theme = t
            and r.date between :fromDate and :toDate
            group by t.id
            order by count(r.id) desc
            """)
    List<Theme> findPopularThemeByDate(LocalDate fromDate, LocalDate toDate, Pageable pageable);
}
