package roomescape.time.application.service;

import roomescape.time.domain.ReservationTime;
import roomescape.time.domain.ReservationTimeId;

import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeQueryService {

    ReservationTime get(ReservationTimeId id);

    List<ReservationTime> getAll();

    boolean existsByStartAt(LocalTime time);

    boolean existById(ReservationTimeId id);
}
