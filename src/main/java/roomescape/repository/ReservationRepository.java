package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSchedule;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsBySchedule(final ReservationSchedule reservationSchedule);

    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Reservation r
                WHERE r.schedule.reservationTime.id = :timeId
            ) THEN TRUE ELSE FALSE END
            """)
    boolean existsByReservationTimeId(@Param("timeId") final Long timeId);

    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Reservation r
                WHERE r.schedule.theme.id = :themeId
            ) THEN TRUE ELSE FALSE END
            """)
    boolean existsByThemeId(@Param("themeId") final Long themeId);

    @Query("""
            SELECT CASE WHEN EXISTS (
                SELECT 1 FROM Reservation r
                WHERE r.schedule.reservationDate.date = :date
                  AND r.schedule.reservationTime.id = :timeId
                  AND r.schedule.theme.id = :themeId
                  AND r.member.id = :memberId
            ) THEN TRUE ELSE FALSE END
            """)
    boolean existsByScheduleAndMemberId(
            @Param("date") final LocalDate date,
            @Param("timeId") final Long timeId,
            @Param("themeId") final Long themeId,
            @Param("memberId") final Long memberId
    );

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId")
    List<Reservation> findByMemberId(@Param("memberId") final Long memberId);
}
