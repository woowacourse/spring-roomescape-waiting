package roomescape.domain.theme;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    default Theme getByIdentifier(Long id) {
        return findById(id)
                .orElseThrow(() -> new NoSuchElementException("해당 id의 테마가 존재하지 않습니다."));
    }

    @Query("""
                SELECT
                    th
                FROM Theme AS th
                JOIN Reservation AS r
                ON th.id = r.theme.id
                WHERE r.date BETWEEN :startDate AND :endDate
                GROUP BY th.id
                ORDER BY COUNT(th.id) DESC
                LIMIT :limit
            """)
    List<Theme> findPopularThemes(LocalDate startDate, LocalDate endDate, int limit);

    boolean existsByName(String name);
}
