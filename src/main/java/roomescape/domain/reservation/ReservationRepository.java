package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByThemeScheduleTimeId(@Param("timeId") Long reservationTimeId);

    boolean existsByThemeSchedule(@Param("themeSchedule") ThemeSchedule themeSchedule);

    boolean existsByThemeScheduleThemeId(@Param("themeId") Long themeId);

    List<Reservation> findByThemeScheduleThemeIdAndThemeScheduleDate(@Param("themeId") Long themeId,
                                                                     @Param("date") LocalDate reservationDate);

    List<Reservation> findByThemeScheduleThemeIdAndMemberIdAndThemeScheduleDateBetween(@Param("themeId") Long themeId,
                                                                                       @Param("memberId") Long memberId,
                                                                                       @Param("from") LocalDate from,
                                                                                       @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"member", "themeSchedule.time"})
    List<Reservation> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT r FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.themeSchedule.time t
                JOIN FETCH r.themeSchedule.theme th
            """)
    List<Reservation> findAllWithMemberAndTimeAndTheme();

    boolean existsByThemeScheduleAndMemberId(@Param("themeSchedule") ThemeSchedule themeSchedule,
                                             @Param("memberId") Long memberId);
}
