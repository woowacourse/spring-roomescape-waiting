package roomescape.reservation.domain.repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.reservation.domain.ReservationTime;

public interface ReservationTimeRepository extends JpaRepository<ReservationTime, Long> {

    boolean existsByStartAt(LocalTime time);

    @Query("""
                SELECT rt FROM MemberReservation mr
                JOIN mr.reservation r 
                JOIN r.time rt 
                WHERE r.date = :date AND r.theme.id = :themeId
            """)
    Set<ReservationTime> findReservedTime(LocalDate date, long themeId);
}
