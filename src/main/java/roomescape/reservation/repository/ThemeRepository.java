package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import roomescape.reservation.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    Optional<Theme> findById(Long id);

    @Query(nativeQuery = true, value = """
                SELECT t.id, t.name, t.description, t.thumbnail
                FROM theme as t
                INNER JOIN reservation as r
                ON
                    t.id = r.theme_id
                    AND r.date BETWEEN :startDate AND :endDate
                GROUP BY t.id
                ORDER BY COUNT(*) DESC
                LIMIT :rowCount
            """)
    List<Theme> findTopByDateAndCount(LocalDate startDate, LocalDate endDate, int rowCount);
}
