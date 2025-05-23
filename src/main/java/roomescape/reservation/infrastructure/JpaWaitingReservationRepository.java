package roomescape.reservation.infrastructure;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationDate;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.theme.domain.Theme;
import roomescape.time.domain.ReservationTime;

public interface JpaWaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {

    @Query("""
            SELECT COALESCE(MAX(w.waitingOrder), 0) FROM WaitingReservation w WHERE
                w.date = :date AND
                w.time = :time AND
                w.theme = :theme
            """)
    int findMaxWaitingByParams(ReservationDate date, ReservationTime time, Theme theme);
}

