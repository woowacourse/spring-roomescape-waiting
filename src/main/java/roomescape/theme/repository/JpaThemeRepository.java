package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.theme.domain.Theme;

public interface JpaThemeRepository extends ListCrudRepository<Theme, Long> {


    @Query("""
            SELECT th
            FROM   Theme th
            LEFT   JOIN Reservation r
              ON   th.id = r.theme.id
             AND   r.date BETWEEN :fromDate AND :toDate
            GROUP  BY th
            ORDER  BY COUNT(r.id) DESC, th.name ASC
            """)
    List<Theme> findPopularThemesWithinDateRange(@Param("fromDate") LocalDate fromDate,
                                                 @Param("toDate") LocalDate toDate, Pageable pageable);
}
