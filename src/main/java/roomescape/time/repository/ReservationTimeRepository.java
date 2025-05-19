package roomescape.time.repository;

import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.domain.ReservationDate;
import roomescape.time.controller.response.AvailableReservationTimeResponse;
import roomescape.time.domain.ReservationTime;

public interface ReservationTimeRepository {

    List<AvailableReservationTimeResponse> findAllAvailableReservationTimes(
            ReservationDate reservationDate,
            Long themeId
    );

    Optional<ReservationTime> findById(Long id);

    List<ReservationTime> findAll();

    void deleteById(Long id);

    ReservationTime save(ReservationTime reservationTime);

    boolean existsByStartAt(LocalTime startAt);
}
