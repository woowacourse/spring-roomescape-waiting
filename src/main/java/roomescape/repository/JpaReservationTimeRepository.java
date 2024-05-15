package roomescape.repository;

import java.time.LocalTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.reservation.TimeNotFoundException;

@Repository
public interface JpaReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    default ReservationTime fetchById(long timeId) {
        return findById(timeId).orElseThrow(TimeNotFoundException::new);
    }

    @Query(value = """
            SELECT
            t.id AS time_id,
            t.start_at AS time_value
            FROM reservation AS r
            INNER JOIN reservation_time AS t ON r.time_id = t.id
            INNER JOIN theme AS th ON r.theme_id = th.id
            WHERE r.date = :date
            AND r.theme_id = :themeId
            """, nativeQuery = true)
    List<ReservationTime> findReservedTimeByThemeAndDate(String date, long themeId);
}
