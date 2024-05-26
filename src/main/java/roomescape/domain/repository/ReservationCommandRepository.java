package roomescape.domain.repository;

import org.springframework.data.repository.Repository;
import roomescape.domain.Reservation;

public interface ReservationCommandRepository extends Repository<Reservation, Long> {

    Reservation save(Reservation reservation);

    void delete(Reservation reservation);
}
