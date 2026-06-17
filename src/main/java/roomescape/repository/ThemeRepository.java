package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    @Query(
            value = """
                    SELECT th.id AS theme_id, th.name, th.description, th.thumbnail_url, COUNT(r.id) AS reservation_count
                    FROM theme th
                    LEFT JOIN reservation r
                    ON r.theme_id = th.id
                    AND r.reservation_date BETWEEN :startDate AND :endDate
                    GROUP BY th.id, th.name, th.description, th.thumbnail_url
                    ORDER BY reservation_count DESC, th.id ASC
                    LIMIT :limit
                    """,
            nativeQuery = true
    )
    List<Theme> findRanking(
            @Param("startDate") LocalDate startDate,
            @Param("endDate") LocalDate endDate,
            @Param("limit") int limit
    );
}
