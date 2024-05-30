package roomescape.domain.reservationdetail;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDetailRepository {
    ReservationDetail save(ReservationDetail reservationDetail);

    ReservationDetail getByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    Optional<ReservationDetail> findByDateAndTimeAndTheme(LocalDate date, ReservationTime time, Theme theme);

    List<ReservationDetail> findAll();

    void delete(ReservationDetail reservationDetail);
}
