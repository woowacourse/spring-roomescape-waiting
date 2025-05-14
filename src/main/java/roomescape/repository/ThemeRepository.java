package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    @Query("""
            SELECT t
            FROM Theme t
            JOIN Reservation r ON r.theme = t
            WHERE r.date >= :from AND r.date < :to
            GROUP BY t
            ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findTopThemes(@Param("from") LocalDate from, @Param("to") LocalDate to, PageRequest pageable);

}
