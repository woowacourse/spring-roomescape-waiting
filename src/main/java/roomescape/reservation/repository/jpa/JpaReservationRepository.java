package roomescape.reservation.repository.jpa;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.schedule.domain.Schedule;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByScheduleTimeId(Long id);

    @Query("""
                SELECT r FROM Reservation r
                WHERE (:themeId IS NULL OR r.schedule.theme.id = :themeId)
                  AND (:memberId IS NULL OR r.member.id = :memberId)
                  AND ((:dateFrom IS NULL OR r.schedule.date >= :dateFrom) AND (:dateTo IS NULL OR r.schedule.date <= :dateTo))
            """)
    List<Reservation> findByMemberAndThemeAndDateBetween(
            Long themeId,
            Long memberId,
            LocalDate dateFrom,
            LocalDate dateTo
    );

    List<Reservation> findAllByMember(Member member);

    boolean existsByMemberAndSchedule(Member member, Schedule schedule);

    boolean existsBySchedule(Schedule schedule);
}
