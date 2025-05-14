package roomescape.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.business.model.entity.Theme;
import roomescape.business.model.vo.Id;
import roomescape.business.model.vo.ReservationDate;
import roomescape.business.model.vo.StartTime;

public interface JpaThemeDao extends JpaRepository<Theme, Id> {

    @Query("""
            SELECT t
              FROM Reservation r
              JOIN r.theme t
             WHERE r.date.value BETWEEN :start AND :end
             GROUP BY t
             ORDER BY COUNT(r) DESC
            """)
    List<Theme> findPopularThemes(
            @Param("start") LocalDate start,
            @Param("end") LocalDate end,
            Pageable pageable
    );
}
