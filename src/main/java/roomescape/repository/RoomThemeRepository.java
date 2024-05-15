package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.RoomTheme;

public interface RoomThemeRepository extends JpaRepository<RoomTheme, Long>, JpaSpecificationExecutor<RoomTheme> {

    @Query("SELECT t FROM RoomTheme as t "
            + "INNER JOIN Reservation as r ON t.id = r.theme.id "
            + "WHERE r.date BETWEEN :dateFrom AND :dateTo "
            + "GROUP BY t.id "
            + "ORDER BY COUNT(t.id) DESC ")
    List<RoomTheme> findAllRanking(@Param("dateFrom") LocalDate dateFrom, @Param("dateTo") LocalDate dateTo, Pageable pageable);
}
