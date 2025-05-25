package roomescape.domain.reservation;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.member.Member;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReservationSlotTimeId(@Param("timeId") Long reservationTimeId);

    boolean existsByReservationSlot(@Param("reservationSlot") ReservationSlot reservationSlot);

    boolean existsByReservationSlotThemeId(@Param("themeId") Long themeId);

    List<Reservation> findByReservationSlotThemeIdAndReservationSlotDate(@Param("themeId") Long themeId,
                                                                         @Param("date") LocalDate reservationDate);

    List<Reservation> findByReservationSlotThemeIdAndMemberIdAndReservationSlotDateBetween(
            @Param("themeId") Long themeId,
            @Param("memberId") Long memberId,
            @Param("frm") LocalDate from,
            @Param("to") LocalDate to);

    @EntityGraph(attributePaths = {"member", "reservationSlot.time"})
    List<Reservation> findAllByMemberId(@Param("memberId") Long memberId);

    @Query("""
            SELECT r FROM Reservation r
                JOIN FETCH r.member m
                JOIN FETCH r.reservationSlot.time t
                JOIN FETCH r.reservationSlot.theme th
            """)
    List<Reservation> findAllWithMemberAndTimeAndTheme();

    boolean existsByReservationSlotAndMember(@Param("reservationSlot") ReservationSlot reservationSlot,
                                             @Param("member") Member member);

    @Query("SELECT r.reservationSlot FROM Reservation r WHERE r.id = :reservationId")
    Optional<ReservationSlot> findThemeScheduleById(@Param("reservationId") Long reservationId);
}
