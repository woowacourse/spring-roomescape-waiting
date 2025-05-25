package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.theme.entity.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    @Query("""
            SELECT t
            FROM Theme t
            LEFT JOIN Reservation r ON r.reservationSlot.theme = t AND r.reservationSlot.date BETWEEN :startDate AND :endDate
            GROUP BY t
            ORDER BY COUNT(r) DESC, t.id DESC
            LIMIT :limit
            """)
    List<Theme> findPopularDescendingUpTo(LocalDate startDate, LocalDate endDate, int limit);

    boolean existsByName(String name);
}
