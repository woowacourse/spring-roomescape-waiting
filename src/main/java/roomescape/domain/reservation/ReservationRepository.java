package roomescape.domain.reservation;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.schedule.ReservationDate;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByScheduleDateAndScheduleTimeIdAndThemeId(ReservationDate scheduleDate, long scheduleTimeId,
                                                            long themeId);

    boolean existsByScheduleTimeId(long timeId);

    boolean existsByThemeId(long themeId);

    @Query("""
            SELECT r FROM Reservation AS r
            WHERE (:memberId is null or r.member.id = :memberId)
            AND (:themeId is null or r.theme.id = :themeId)
            AND (:dateFrom is null or r.schedule.date >= :dateFrom)
            AND (:dateTo is null or r.schedule.date < :dateTo)""")
    List<Reservation> findBy(@Param("memberId") Long memberId, @Param("themeId") Long themeId,
                             @Param("dateFrom") ReservationDate dateFrom, @Param("dateTo") ReservationDate dateTo);

    List<Reservation> findByScheduleDateAndThemeId(ReservationDate date, long themeId);

    List<Reservation> findByMemberId(long memberId);
}
