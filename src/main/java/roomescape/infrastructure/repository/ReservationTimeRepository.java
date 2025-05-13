package roomescape.infrastructure.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.presentation.dto.response.AvailableReservationTimeResponse;
import roomescape.domain.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    List<AvailableReservationTimeResponse> findAllAvailableReservationTimes(LocalDate date, Long themeId);
}
