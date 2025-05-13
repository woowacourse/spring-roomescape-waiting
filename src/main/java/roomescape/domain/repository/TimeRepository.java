package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import roomescape.domain.ReservationTime;
import roomescape.domain.repository.dto.TimeDataWithBookingInfo;

public interface TimeRepository {
    Long save(ReservationTime reservationTime);

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    boolean deleteById(Long id);

    List<TimeDataWithBookingInfo> getTimesWithBookingInfo(LocalDate date, Long themeId);
}
