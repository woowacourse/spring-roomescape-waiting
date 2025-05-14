package roomescape.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import roomescape.entity.ReservationTime;

public interface ReservationTimeRepository {

    List<ReservationTime> findAll();

    List<ReservationTime> findAllTimesWithBooked(LocalDate date, Long themeId);

    Optional<ReservationTime> findById(Long id);

    boolean existsByStartAt(LocalTime startAt);

    ReservationTime save(ReservationTime reservationTime);

    void deleteById(Long id);
}
