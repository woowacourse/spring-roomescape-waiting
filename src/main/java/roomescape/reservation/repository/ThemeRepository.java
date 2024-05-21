package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.Theme;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {


    Optional<Theme> findByThemeName(String name);

    @Query("""
            select th from Theme as th
            left outer join Reservation r
                on r.theme = th and r.date >= :start and r.date <= :end
            group by th.id, th.themeName, th.description, th.thumbnail
            order by count(r.id) desc
            """)
    List<Theme> findPopularThemesWithPagination(
            @Param(value = "start") LocalDate startDate,
            @Param(value = "end") LocalDate endDate,
            Pageable pageable
    );
}
