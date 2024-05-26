package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
                SELECT t
                FROM Theme t
                JOIN Reservation r ON r.theme.id = t.id
                JOIN MemberReservation mr ON mr.reservation.id = r.id
                WHERE r.date BETWEEN :startDate AND :endDate
                GROUP BY t.id, t.name
                ORDER BY COUNT(*) DESC
            """)
    List<Theme> findTopThemesByReservations(LocalDate startDate, LocalDate endDate, Pageable pageable);
}
