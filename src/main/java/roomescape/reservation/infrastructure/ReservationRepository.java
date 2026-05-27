package roomescape.reservation.infrastructure;

import roomescape.reservation.Reservation;
import roomescape.reservation.infrastructure.projection.ReservationDetailProjection;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface ReservationRepository {
    Reservation save(Reservation reservation);

    List<ReservationDetailProjection> findAll();

    Set<Long> findTimeIdByDateAndThemeId(LocalDate date, long themeId);

    List<ReservationDetailProjection> findAllReservationDetailsByMemberId(long memberId);

    void deleteById(long reservationId);

    Optional<ReservationDetailProjection> findDetailById(long reservationId);

    boolean existsByScheduleIdAndIdNot(long scheduleId, long reservationId);

    int updateScheduleById(long reservationId, long scheduleId);

    Optional<Reservation> findById(long reservationId);

    boolean existsByScheduleId(long scheduleId);

    boolean existsByMemberIdAndScheduleId(long memberId, long scheduleId);
}
