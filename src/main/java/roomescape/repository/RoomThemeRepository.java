package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.RoomTheme;

public interface RoomThemeRepository extends JpaRepository<RoomTheme, Long> {

    @Query("SELECT t.id, t.name, t.description, t.thumbnail FROM RoomTheme as t "
            + "INNER JOIN Reservation as r ON t.id = r.theme.id "
            + "WHERE :fromDate < r.date AND r.date < :toDate "
            + "GROUP BY t.id "
            + "ORDER BY COUNT(t.id) DESC")
    List<RoomTheme> findAllRanking(@Param("fromDate") LocalDate fromDate, @Param("toDate") LocalDate toDate);
}
