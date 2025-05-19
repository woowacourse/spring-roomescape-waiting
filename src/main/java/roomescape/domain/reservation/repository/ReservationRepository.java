package roomescape.domain.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByTimeId(@Param("timeId") Long reservationTimeId);

    boolean existsByDateAndTimeIdAndThemeId(@Param("date") LocalDate reservationDate,
                                            @Param("timeId") Long timeId,
                                            @Param("themeId") Long themeId);

    boolean existsByThemeId(@Param("themeId") Long themeId);

    List<Reservation> findByThemeIdAndDate(@Param("themeId") Long themeId, @Param("date") LocalDate reservationDate);

    List<Reservation> findByThemeIdAndMemberIdAndDateBetween(@Param("themeId") Long themeId,
                                                             @Param("memberId") Long memberId,
                                                             @Param("from") LocalDate from,
                                                             @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"time", "theme"})
    List<Reservation> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT r FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.time t
                JOIN FETCH r.theme th
            """)
    List<Reservation> findAllWithMemberAndTimeAndTheme();
}
