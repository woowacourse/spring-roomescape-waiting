package roomescape.reservation.domain;

import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeQueryRepository {

    ReservationTime getByIdOrThrow(Long id);

    List<ReservationTime> findAllByStartAt(LocalTime startAt);

    List<ReservationTime> findAll();
}
