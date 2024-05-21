package roomescape.domain.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

public interface ReservationStatusRepository {

    ReservationStatus save(ReservationStatus reservationStatus);

    ReservationStatus getById(long id);

    void deleteById(Long id);

    Optional<ReservationStatus> findFirstWaitingBy(Theme theme, LocalDate date, ReservationTime time);

    long getWaitingCount(Theme theme, LocalDate date, ReservationTime time, LocalDateTime createdAt);
}
