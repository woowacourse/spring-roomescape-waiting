package roomescape.reservationtime.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.reservationtime.domain.ReservationTime;
import roomescape.reservationtime.dto.AvailableReservationTimeResponse;

public interface ReservationTimeRepository {

    ReservationTime save(final ReservationTime reservationTime);

    List<ReservationTime> findAll();

    int deleteById(final Long id);

    List<AvailableReservationTimeResponse> findAllAvailable(final LocalDate date, final Long themeId);

    Optional<ReservationTime> findById(final Long id);
}
