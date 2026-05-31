package roomescape.domain.reservationdate;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReservationDateRepository {

    Optional<ReservationDate> findById(Long id);

    List<ReservationDate> findAll();

    ReservationDate save(ReservationDate reservationDate);

    int deleteById(Long id);

    boolean existsByPlayDay(LocalDate playDay);
}
