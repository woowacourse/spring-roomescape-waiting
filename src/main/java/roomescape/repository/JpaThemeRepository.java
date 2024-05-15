package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.Theme;

@Repository
public interface JpaThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT th 
            FROM Theme th 
            LEFT JOIN Reservation r ON th.id = r.theme.id 
            WHERE r.date 
            BETWEEN :startDate AND :endDate 
            GROUP BY th.id 
            ORDER BY count(r) desc
            """)
    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate);

    default Theme fetchById(long themeId) {
        return findById(themeId).orElseThrow(() -> new IllegalArgumentException("테마가 존재하지 않습니다."));
    }
}
