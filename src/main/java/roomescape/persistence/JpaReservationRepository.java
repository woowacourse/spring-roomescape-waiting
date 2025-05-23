package roomescape.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.domain.Reservation;
import roomescape.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberId(Long memberId);

    boolean existsBySchedule_TimeId(Long reservationTimeId);

    boolean existsBySchedule(Schedule schedule);

    boolean existsBySchedule_ThemeId(Long themeId);

    List<Reservation> findBySchedule_ThemeIdAndSchedule_Date(Long themeId, LocalDate reservationDate);

    @Query("""
        SELECT r
        FROM Reservation r
        JOIN r.schedule.time t
        JOIN r.schedule.theme tm
        JOIN r.member m
        WHERE (:memberId IS NULL OR r.member.id = :memberId)
        AND (:themeId IS NULL OR r.schedule.theme.id = :themeId)
        AND (:dateFrom IS NULL OR r.schedule.date >= :dateFrom)
        AND (:dateTo IS NULL OR r.schedule.date <= :dateTo)
        ORDER BY r.id
    """)
    List<Reservation> findReservationsInConditions(final Long memberId, final Long themeId, final LocalDate dateFrom, final LocalDate dateTo);
}
