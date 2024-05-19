package roomescape.infrastructure;


import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.Theme;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    @Query(value = """
                    SELECT
                        th,
                        COUNT(r.theme.id) AS count
                    FROM Theme AS th
                    LEFT JOIN Reservation AS r
                    ON th.id = r.theme.id
                    AND r.date.date BETWEEN :from AND :to
                    GROUP BY th.id
                    ORDER BY count DESC
            """)
    List<Theme> findMostReservedThemesInPeriod(
            @Param("from") LocalDate from,
            @Param("to") LocalDate to,
            Pageable pageable);
}
