package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    default Theme getByIdOrThrow(Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
    }

    @Query("""
                SELECT t
                FROM Reservation r
                JOIN r.theme t
                WHERE r.date BETWEEN :from AND :to
                GROUP BY t
                ORDER BY COUNT(r.id) DESC
            """)
    List<Theme> findTopNThemesByReservationCountInDateRange(
            final @Param("from") LocalDate dateFrom,
            final @Param("to") LocalDate dateTo,
            final Pageable pageable);

    default List<Theme> getTopNThemesInPeriod(LocalDate dateFrom, LocalDate dateTo, int limit) {
        int FIRST_PAGE = 0;

        Pageable topThemePageable = PageRequest.of(FIRST_PAGE, limit);
        return findTopNThemesByReservationCountInDateRange(dateFrom, dateTo, topThemePageable);
    }
}
