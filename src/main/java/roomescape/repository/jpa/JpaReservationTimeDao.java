package roomescape.repository.jpa;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.ReservationTime;

public interface JpaReservationTimeDao extends JpaRepository<ReservationTime, Long> {
    boolean existsByStartAt(LocalTime startAt);

    @Query(nativeQuery = true, value = """
                    SELECT
                        rt.id, start_at
                    FROM reservation_time rt
                    JOIN reservation r
                        ON rt.id = r.time_id
                    WHERE r.date = :date
                    AND r.theme_id = :themeId
            """)
    List<ReservationTime> findUsedTimeByDateAndThemeId(LocalDate date, long themeId);
}
