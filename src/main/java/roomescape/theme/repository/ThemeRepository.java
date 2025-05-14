package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    String sql =
            "SELECT th "
                    + "FROM Theme th "
                    + "left join Reservation as r "
                    + "on th.id = r.theme.id "
                    + "and r.date between ?1 and ?2 "
                    + "group by th.id "
                    + "order by count(r.id) desc, "
                    + "th.name asc "
                    + "limit 10";
    @Query(sql)
    List<Theme> findTop10PopularThemesWithinLastWeek(LocalDate fromDate, LocalDate toDate);
}
