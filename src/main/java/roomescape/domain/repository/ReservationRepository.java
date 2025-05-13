package roomescape.domain.repository;

import java.util.List;
import roomescape.domain.Reservation;
import roomescape.domain.repository.dto.ReservationSearchFilter;

public interface ReservationRepository {
    List<Reservation> findAll();

    Long save(Reservation reservation);

    boolean deleteById(Long id);

    List<Reservation> searchWith(ReservationSearchFilter reservationSearchFilter);
}
