package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationStatusRepository {

    ReservationStatus save(ReservationStatus reservationStatus);

    ReservationStatus getById(long id);

    void deleteById(Long id);

    Optional<ReservationStatus> findFirstWaitingBy(Theme theme, LocalDate date, ReservationTime time);

    long getWaitingCount(Reservation reservation);
}
