package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import roomescape.domain.reservation.ReservationTime;
import roomescape.repository.dto.AvailableReservationTimeResult;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

    @Query(value = """
                    SELECT
                        r.id IS NOT NULL AS isBooked,
                        rt.id AS timeId,
                        rt.start_at AS startAt
                    FROM Reservation_Time AS rt
                    LEFT OUTER JOIN Reservation AS r
                    ON rt.id = r.time_id AND r.date = ? AND r.theme_id = ?
            """, nativeQuery = true)
    List<AvailableReservationTimeResult> getAvailableReservationTimeByThemeIdAndDate(LocalDate date, Long themeId);

    @Transactional
    long deleteReservationTimeById(long timeId);
}
