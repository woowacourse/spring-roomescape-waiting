package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;

import roomescape.domain.Theme;

public interface ThemeRepository extends Repository<Theme, Long> {
    Theme save(Theme theme);

    Optional<Theme> findById(Long id);

    @Query(value = """
            SELECT new roomescape.domain.Theme( t.id, t.name, t.description, t.thumbnail)
            FROM Reservation r
            LEFT JOIN r.theme t
            WHERE r.date >= :start AND r.date <= :end
            GROUP BY t.id, t.name, t.description, t.thumbnail
            ORDER BY COUNT(r) DESC
            """)
    List<Theme> findThemeByPeriodWithLimit(@Param("start") LocalDate start, @Param("end") LocalDate end,
                                           Pageable pageable);

    List<Theme> findAll();

    void delete(Theme theme);

    void deleteAll();
}
