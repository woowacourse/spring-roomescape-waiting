package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.entity.Member;
import roomescape.reservation.entity.Reservation;
import roomescape.reservation.entity.ReservationSlot;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    Optional<Reservation> findByReservationSlot(ReservationSlot reservationSlot);

    List<Reservation> findAllByMember(Member member);

    @Query("""
            SELECT r FROM Reservation r
            WHERE r.reservationSlot.theme.id = :themeId
            AND r.member.id = :memberId
            AND r.reservationSlot.date BETWEEN :dateFrom AND :dateTo
            """)
    List<Reservation> findReservationsInPeriod(Long themeId, Long memberId, LocalDate dateFrom, LocalDate dateTo);

    boolean existsByReservationSlot_TimeId(Long reservationSlotTimeId);

    boolean existsByReservationSlot_ThemeId(Long themeId);

    boolean existsByReservationSlot(ReservationSlot reservationSlot);

    boolean existsByReservationSlotAndMemberId(ReservationSlot reservationSlot, Long memberId);

    void deleteByReservationSlot(ReservationSlot reservationSlot);
}
