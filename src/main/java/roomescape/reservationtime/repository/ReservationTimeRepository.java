package roomescape.reservationtime.repository;

import roomescape.reservationtime.domain.ReservationTime;

import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long timeId);

    ReservationTime save(ReservationTime newReservationTime);

    boolean delete(Long timeId);
}
