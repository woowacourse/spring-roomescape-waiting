package roomescape.time.domain;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.time.dto.ReservationTimeWithBookStatusResponse;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    Optional<ReservationTime> findById(Long id);

    List<ReservationTimeWithBookStatusResponse> findByDateAndThemeIdWithBookStatus(LocalDate date, Long themeId);

    ReservationTime save(ReservationTime reservationTime);

    boolean existByStartAt(LocalTime startAt);

    void deleteById(Long id);
}
