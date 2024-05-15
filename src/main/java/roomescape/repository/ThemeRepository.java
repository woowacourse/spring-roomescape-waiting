package roomescape.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import roomescape.model.Theme;

public interface ThemeRepository extends CrudRepository<Theme, Long> {

    List<Theme> findAll();

    Theme save(Theme theme); // save

    void deleteById(long id); // delete

    Theme findById(long id);

    @Query("""
            SELECT r.theme
            FROM Reservation r
            INNER JOIN Theme th on r.theme.id = th.id
            WHERE r.date BETWEEN :before AND :after
            GROUP BY th
            ORDER BY COUNT(th) DESC
            limit 10
            """)
    List<Theme> findFirst10ByDateBetweenOrderByTheme(@Param("before") LocalDate before,
                                                     @Param("after") LocalDate after);
}
