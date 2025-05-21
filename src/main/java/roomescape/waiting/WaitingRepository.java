package roomescape.waiting;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.Reservation;
import roomescape.reservationtime.ReservationTime;
import roomescape.theme.Theme;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    Optional<Waiting> findByReservation(Reservation reservation);

    List<Waiting> findAll();

    @Query(value = """
                SELECT w FROM Waiting w
                WHERE w.reservation.date = :date
                AND w.reservation.reservationTime = :time
                AND w.reservation.theme = :theme
                AND w.rank > :rank
            """)
    List<Waiting> findWaitingGreaterThanRank(LocalDate date, ReservationTime time, Theme theme, Long rank);

}
