package roomescape.domain.reservationitem;

import java.time.LocalDate;
import java.util.Optional;

public interface ReservationItemRepository {

    ReservationItem save(ReservationItem reservationItem);

    void delete(ReservationItem reservationItem);

    Optional<ReservationItem> findReservationItemByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme);

    boolean existsByDateAndTimeAndTheme(LocalDate date, ReservationTime time, ReservationTheme theme);
}
