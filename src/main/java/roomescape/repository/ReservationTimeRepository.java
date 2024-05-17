package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.domain.reservation.ReservationTime;
import roomescape.exception.reservation.TimeNotFoundException;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime startAt);

    @Query(value = """
            SELECT rt
            FROM ReservationTime rt
            INNER JOIN Reservation r ON rt.id = r.time.id
            INNER JOIN Theme th ON th.id = r.theme.id
            WHERE r.date = :date
            AND r.theme.id = :themeId
            """)
    Set<ReservationTime> findReservedTime(LocalDate date, long themeId);

    default ReservationTime fetchById(long timeId) {
        return findById(timeId).orElseThrow(TimeNotFoundException::new);
    }
}
