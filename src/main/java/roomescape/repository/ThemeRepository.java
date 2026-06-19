package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("SELECT t FROM Theme t JOIN Reservation r ON r.slot.theme = t WHERE r.slot.date BETWEEN :startDate AND :endDate GROUP BY t ORDER BY COUNT(r) DESC LIMIT 10")
    List<Theme> findPopularThemesByPeriod(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate);

    boolean existsByName(String name);
}
