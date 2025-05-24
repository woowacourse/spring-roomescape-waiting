package roomescape.repository;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.ListCrudRepository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationDate;
import roomescape.domain.time.ReservationTime;

public interface ReservationRepository extends ListCrudRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {

    boolean existsByScheduleReservationDateAndScheduleReservationTime(
            final ReservationDate reservationDate,
            final ReservationTime reservationTime
    );

    @Query("SELECT r.id FROM Reservation r WHERE r.schedule.reservationTime.id = :timeId")
    List<Long> findIdsByReservationTimeId(@Param("timeId") final Long timeId, final Pageable pageable);

    @Query("SELECT r.id FROM Reservation r WHERE r.schedule.theme.id = :themeId")
    List<Long> findIdsByThemeId(@Param("themeId") final Long themeId, final Pageable pageable);

    default boolean existsByReservationTimeId(final Long timeId) {
        return !findIdsByReservationTimeId(timeId, Pageable.ofSize(1)).isEmpty();
    }

    default boolean existsByThemeId(final Long themeId) {
        return !findIdsByThemeId(themeId, Pageable.ofSize(1)).isEmpty();
    }

    @Query("SELECT r FROM Reservation r WHERE r.member.id = :memberId")
    List<Reservation> findByMemberId(@Param("memberId") final Long memberId);
}
