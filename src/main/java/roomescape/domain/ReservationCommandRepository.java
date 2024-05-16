package roomescape.domain;

import org.springframework.data.repository.Repository;

public interface ReservationCommandRepository extends Repository<Reservation, Long> {

    Reservation save(Reservation reservation);

    void deleteById(Long id);
}
