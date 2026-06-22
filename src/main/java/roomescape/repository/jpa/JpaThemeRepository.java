package roomescape.repository.jpa;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.repository.result.PopularThemeResult;

public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            select new roomescape.repository.result.PopularThemeResult(
                theme.id,
                theme.name,
                theme.description,
                theme.thumbnail,
                count(reservation)
            )
            from Theme theme
            left join Reservation reservation
                on reservation.theme = theme
                and reservation.date between :startDate and :endDate
            group by
                theme.id,
                theme.name,
                theme.description,
                theme.thumbnail
            order by
                count(reservation) desc,
                theme.id asc
            """)
    List<PopularThemeResult> findPopular(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            Pageable pageable
    );
}
