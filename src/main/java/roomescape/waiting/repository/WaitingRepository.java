package roomescape.waiting.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.ReservationSlot;
import roomescape.reservation.entity.ReservationTime;
import roomescape.theme.entity.Theme;
import roomescape.waiting.entity.Waiting;
import roomescape.waiting.entity.WaitingWithRank;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

//    long countByDateAndThemeAndMember(LocalDate date, Theme theme, Member member);

    long countByReservationSlot_DateAndReservationSlot_ThemeAndMember(
            LocalDate date, Theme theme, Member member);

    @Query("""
            SELECT new roomescape.waiting.entity.WaitingWithRank(
                           w,
                           (SELECT COUNT(w2)
                            FROM Waiting w2
                            WHERE w2.reservationSlot.theme = w.reservationSlot.theme
                              AND w2.reservationSlot.date = w.reservationSlot.date
                              AND w2.reservationSlot.time = w.reservationSlot.time
                              AND w2.id < w.id))
                       FROM Waiting w
                       WHERE w.member.id = :memberId
            """)
    List<WaitingWithRank> findWaitingsWithRankByMemberId(Long memberId);

//    boolean existsByDateAndTimeIdAndThemeIdAndMemberId(LocalDate date, Long theme, Long timeId, Long memberId);

    boolean existsByReservationSlot_DateAndReservationSlot_ThemeIdAndReservationSlot_TimeIdAndMemberId(
            LocalDate reservationSlotDate, Long reservationSlotThemeId, Long reservationSlotTimeId, Long memberId);

//    Optional<Waiting> findFirstByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

    Optional<Waiting> findFirstByReservationSlot_DateAndReservationSlot_ThemeAndReservationSlot_Time(
            LocalDate reservationSlotDate, Theme reservationSlotTheme, ReservationTime reservationSlotTime);

    Optional<Waiting> findFirstByReservationSlot(ReservationSlot reservationSlot);

//    boolean existsByDateAndThemeAndTime(LocalDate date, Theme theme, ReservationTime time);

    boolean existsByReservationSlot_DateAndReservationSlot_ThemeAndReservationSlot_Time(
            LocalDate reservationSlotDate, Theme reservationSlotTheme, ReservationTime reservationSlotTime);

    boolean existsByReservationSlot(ReservationSlot reservationSlot);
}
