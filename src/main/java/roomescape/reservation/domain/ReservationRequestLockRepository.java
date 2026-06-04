package roomescape.reservation.domain;

import java.time.LocalDate;

public interface ReservationRequestLockRepository {

    void lock(String name, LocalDate date, Long timeId);
}
