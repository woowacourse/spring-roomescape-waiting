package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("SELECT t FROM Theme t WHERE t.id IN (SELECT r.theme.id FROM Reservation r WHERE r.date BETWEEN :startDate AND :endDate GROUP BY r.theme.id ORDER BY COUNT(r.theme.id) DESC)")
    List<Theme> findThemesWithReservationsBetweenDates(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
