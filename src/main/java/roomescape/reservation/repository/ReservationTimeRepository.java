package roomescape.reservation.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationTime;
import roomescape.reservation.repository.dto.ReservationTimeWithBooked;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    Optional<ReservationTime> findById(Long id);

    Boolean existsByStartAt(LocalTime startAt);

    @Query("""
    SELECT new roomescape.reservation.repository.dto.ReservationTimeWithBooked(
        t,
        CASE WHEN COUNT(r) > 0 THEN true ELSE false END
    )
    FROM ReservationTime t
    LEFT JOIN Reservation r
    ON
        r.reservationInformation.time = t
        AND r.reservationInformation.date = :date
        AND r.reservationInformation.theme.id = :themeId
    GROUP BY t
    """)
    List<ReservationTimeWithBooked> findAllWithBooked(LocalDate date, Long themeId);
}
