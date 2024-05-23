package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long>, JpaSpecificationExecutor<Theme> {

    @Query("SELECT t FROM Theme as t "
            + "INNER JOIN Reservation as r ON t.id = r.theme.id "
            + "WHERE r.date BETWEEN :dateFrom AND :dateTo "
            + "GROUP BY t.id "
            + "ORDER BY COUNT(t.id) DESC LIMIT :count ")
    List<Theme> findMostReservedThemeInPeriodByCount(LocalDate dateFrom, LocalDate dateTo, int count);
}
