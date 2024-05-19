package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.exception.DomainNotFoundException;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    boolean existsByName(String name);

    @Query(value = """
                SELECT
                    th.id,
                    th.name,
                    th.description,
                    th.thumbnail
                FROM Theme AS th
                JOIN Reservation AS r
                ON th.id = r.theme_id
                WHERE :startDate <= r.date AND r.date <= :endDate
                GROUP BY th.id
                ORDER BY COUNT(th.id) DESC
                LIMIT :limit
            """, nativeQuery = true)
    List<Theme> findPopularThemes(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit
    );

    default Theme getById(Long id) {
        return findById(id)
                .orElseThrow(() -> new DomainNotFoundException("해당 id의 테마가 존재하지 않습니다."));
    }
}
