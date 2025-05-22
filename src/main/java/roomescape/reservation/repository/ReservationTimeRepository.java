package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.reservation.domain.ReservationTime;

import java.time.LocalTime;
import java.util.Optional;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    Optional<ReservationTime> findById(Long id);

    Boolean existsByStartAt(LocalTime startAt);

    // TODO: jpql
//    List<BookedReservationTimeResponse> findAllWithBooked(LocalDate date, Long themeId);
}
