package roomescape.persistence.repository;

import java.time.LocalTime;
import java.util.List;
import roomescape.model.ReservationTime;

public interface ReservationTimeRepository {

    boolean isDuplicatedStartAt(LocalTime startAt);

    ReservationTime findById(Long id);

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
