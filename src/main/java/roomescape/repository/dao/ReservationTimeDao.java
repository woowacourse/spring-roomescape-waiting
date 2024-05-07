package roomescape.repository.dao;

import roomescape.model.ReservationTime;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeDao {

    long save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(long id);

    void deleteById(long id);

    Boolean isExistById(long id);

    Boolean isExistByStartAt(LocalTime startAt);
}
