package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservation.dto.response.ReservationTimeResponse.AvailableReservationTimeResponse;
import roomescape.reservation.entity.ReservationTime;

public interface ReservationTimeRepository {

    ReservationTime save(ReservationTime time);

    List<ReservationTime> findAll();

    List<AvailableReservationTimeResponse> findAvailableTimes(LocalDate date, Long themeId);

    Optional<ReservationTime> findById(Long id);

    boolean deleteById(Long id);
}
