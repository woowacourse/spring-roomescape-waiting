package roomescape.time.application.service;

import roomescape.time.domain.ReservationTime;

import java.time.LocalTime;
import java.util.List;

public interface ReservationTimeQueryService {

    ReservationTime get(Long id);

    List<ReservationTime> getAll();

    boolean existsByStartAt(LocalTime time);

    boolean existById(Long id);
}
