package roomescape.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationSchedule;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsBySchedule(final ReservationSchedule reservationSchedule);

    @Query("SELECT r.id FROM Reservation r WHERE r.schedule.reservationTime.id = :timeId")
    List<Long> findIdsByReservationTimeId(@Param("timeId") final Long timeId, final Pageable pageable);

    default boolean existsByReservationTimeId(final Long timeId) {
        return !findIdsByReservationTimeId(timeId, Pageable.ofSize(1)).isEmpty();
    }

    @Query("SELECT r.id FROM Reservation r WHERE r.schedule.theme.id = :themeId")
    List<Long> findIdsByThemeId(@Param("themeId") final Long themeId, final Pageable pageable);

    default boolean existsByThemeId(final Long themeId) {
        return !findIdsByThemeId(themeId, Pageable.ofSize(1)).isEmpty();
    }

    @Query("""
                SELECT r.id FROM Reservation r
                WHERE r.schedule.reservationDate.date = :date
                  AND r.schedule.reservationTime.id = :timeId
                  AND r.schedule.theme.id = :themeId
                  AND r.member.id = :memberId
            """)
    List<Long> findIdsByScheduleAndMember(@Param("date") final LocalDate date, @Param("timeId") final Long timeId,
                                          @Param("themeId") final Long themeId, @Param("memberId") final Long memberId,
                                          Pageable pageable);

    default boolean existsByScheduleAndMemberId(final LocalDate date, final Long timeId,
                                                final Long themeId, final Long memberId) {
        return !findIdsByScheduleAndMember(date, timeId, themeId, memberId, Pageable.ofSize(1)).isEmpty();
    }

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId")
    List<Reservation> findByMemberId(@Param("memberId") final Long memberId);
}
