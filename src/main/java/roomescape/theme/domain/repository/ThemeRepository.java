package roomescape.theme.domain.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.global.exception.error.ErrorType;
import roomescape.global.exception.model.NotFoundException;
import roomescape.theme.domain.Theme;

import java.time.LocalDate;
import java.util.List;

public interface ThemeRepository extends JpaRepository<Theme, Long> {

    default Theme getById(final Long id) {
        return findById(id)
                .orElseThrow(() -> new NotFoundException(ErrorType.THEME_NOT_FOUND,
                        String.format("테마(Theme) 정보가 존재하지 않습니다. [themeId: %d]", id)));
    }

    @Query(value = """
            SELECT t
            FROM Reservation r JOIN r.theme t
            WHERE r.date BETWEEN :startDate AND :endDate
            GROUP BY t.id
            ORDER BY COUNT(t) DESC, t.id ASC
            """)
    List<Theme> findTopNThemeBetweenStartDateAndEndDate(@Param("startDate") LocalDate startDate, @Param("endDate") LocalDate endDate, Pageable pageable);
}
