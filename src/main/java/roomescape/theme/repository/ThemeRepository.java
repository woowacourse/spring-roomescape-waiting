package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme t INNER JOIN Reservation r ON (t.id = r.detail.theme.id)
            WHERE r.detail.date BETWEEN :startDate AND :endDate
            GROUP BY t.id
            ORDER BY COUNT(t.id) DESC
            LIMIT :rankLimit
            """)
    List<Theme> findAllByRank(LocalDate startDate, LocalDate endDate, int rankLimit);
}
