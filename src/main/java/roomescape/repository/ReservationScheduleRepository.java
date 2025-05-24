package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.schdule.ReservationSchedule;
import roomescape.domain.time.AvailableReservationTime;

public interface ReservationScheduleRepository extends JpaRepository<ReservationSchedule, Long> {

    Optional<ReservationSchedule> findByReservationTime_IdAndTheme_IdAndReservationDate_Date(Long timeId, Long themeId,
                                                                                             LocalDate date);

    Optional<ReservationSchedule> findByReservationTime_Id(Long timeId);

    Optional<ReservationSchedule> findByTheme_Id(Long themeId);

    @Query("""
            SELECT new roomescape.domain.time.AvailableReservationTime(
                rs,
                CASE WHEN COUNT(r.id) > 0 THEN true ELSE false END
            )
            FROM ReservationSchedule rs
            LEFT JOIN Reservation r ON r.schedule = rs
            WHERE rs.theme.id = :themeId AND rs.reservationDate.date = :date
            GROUP BY rs
            """)
    List<AvailableReservationTime> findAllAvailableReservationSchedules(
            @Param("date") LocalDate date,
            @Param("themeId") Long themeId
    );

}
