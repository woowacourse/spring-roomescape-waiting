package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationSlot(ReservationSlot reservationSlot);

    List<Reservation> findAllByMember(Member member);

    List<Reservation> findAllByReservationSlot_ThemeIdAndMemberIdAndReservationSlot_DateBetween(Long reservationSlot_theme_id,
                                                                              Long member_id,
                                                                              LocalDate dateFrom,
                                                                              LocalDate dateTo);

    boolean existsByReservationSlot_TimeId(Long reservationSlotTimeId);

    boolean existsByReservationSlot_ThemeId(Long themeId);

    boolean existsByReservationSlot_DateAndReservationSlot_TimeIdAndReservationSlot_ThemeId(LocalDate date, Long timeId, Long themeId);

    boolean existsByReservationSlot(ReservationSlot reservationSlot);

    boolean existsByReservationSlot_DateAndReservationSlot_TimeIdAndReservationSlot_ThemeIdAndMemberId(LocalDate date, Long timeId, Long themeId, Long memberId);
}
