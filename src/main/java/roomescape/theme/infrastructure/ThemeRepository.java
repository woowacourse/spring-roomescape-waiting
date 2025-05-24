package roomescape.theme.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.exception.resource.ResourceNotFoundException;
import roomescape.theme.domain.Theme;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    boolean existsByName(String name);

    default Theme getByIdOrThrow(final Long id) {
        return this.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("해당 테마가 존재하지 않습니다."));
    }

    @Query("""
                SELECT t
                FROM ReservationSlot rs
                JOIN rs.theme t
                WHERE rs.date BETWEEN :from AND :to
                GROUP BY t
                ORDER BY COUNT(rs.id) DESC
            """)
    List<Theme> findTopNThemesByReservationCountInDateRange(
            final LocalDate from,
            final LocalDate to,
            final Pageable pageable);

    default List<Theme> getTopNThemesInPeriod(final LocalDate dateFrom, final LocalDate dateTo, final int limit) {
        final int FIRST_PAGE = 0;

        final Pageable topThemePageable = PageRequest.of(FIRST_PAGE, limit);
        return findTopNThemesByReservationCountInDateRange(dateFrom, dateTo, topThemePageable);
    }
}
