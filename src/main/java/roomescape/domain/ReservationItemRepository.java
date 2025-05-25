package roomescape.domain;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationItemRepository {

    ReservationItem save(ReservationItem reservationItem);

    Optional<ReservationItem> findReservationItemByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme);
}
