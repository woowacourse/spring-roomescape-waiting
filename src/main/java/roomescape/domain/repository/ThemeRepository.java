package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Theme;
import roomescape.dto.AvailableTimeResponse;

public interface ThemeRepository extends JpaRepository<Theme, Long> {
    // todo: ReservationSlot을 엔티티로 변경 후 아래 코드는 JPQL로 변환 예정
    @Query(value = """
        SELECT th.*
        FROM reservation_slot rs
        JOIN theme th ON rs.theme_id = th.id
        WHERE rs.date BETWEEN :from AND :to
        GROUP BY th.id, th.name, th.description, th.thumbnail_url
        ORDER BY COUNT(rs.id) DESC
        """, nativeQuery = true)
    List<Theme> findPopularThemes(LocalDate from, LocalDate to, Pageable pageable);

    @Query(value = """
        SELECT
            rt.id AS id,
            rt.start_at AS startAt,
            CASE WHEN COUNT(res.id) = 0 THEN TRUE ELSE FALSE END AS isAvailable,
            GREATEST(COUNT(res.id) - 1, 0) AS waitNumber
        FROM reservation_time rt
        LEFT JOIN reservation_slot rs
            ON rt.id = rs.time_id
            AND rs.theme_id = :themeId
            AND rs.date = :date
        LEFT JOIN reservation res
            ON res.reservation_slot_id = rs.id
            AND res.status != 'CANCELED'
        GROUP BY rt.id, rt.start_at
        ORDER BY rt.start_at
        """, nativeQuery = true)
    List<AvailableTimeResponse> findAvailableTimeById(
            @Param("themeId") long themeId,
            @Param("date") String date
    );
}
