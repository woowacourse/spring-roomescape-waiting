package roomescape.reservation.repository;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.reservation.domain.ReservationTime;

@Repository
public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    @Query("""
            select t
            from ReservationTime t
            join Reservation r
            on r.reservationTime.id = t.id
            where t.id = :id
            """)
    Optional<ReservationTime> findByReservationsId(Long id);
}

