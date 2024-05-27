package roomescape.theme.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import roomescape.exceptions.NotFoundException;
import roomescape.theme.domain.Name;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends ListCrudRepository<Theme, Long> {

    boolean existsByName(Name name);

    @Query(value = """
            SELECT r.theme
            FROM Reservation r
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY r.theme
            ORDER BY COUNT(r.theme) DESC
            """)
    List<Theme> findTrendingThemesBetweenDates(
            LocalDate startDate,
            LocalDate endDate,
            Pageable pageable
    );

    default Theme getById(Long id) {
        return findById(id).orElseThrow(() -> new NotFoundException("존재하지 않는 테마입니다. themeId = " + id));
    }
}
