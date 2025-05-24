package roomescape.reservation.infrastructure;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.Reservation;
import roomescape.reservation.domain.ReservationSlot;
import roomescape.theme.domain.Theme;

public interface JpaReservationRepository extends JpaRepository<Reservation, Long> {

    boolean existsByReservationSlot(ReservationSlot reservationSlot);

    boolean existsByReservationSlotAndMember(ReservationSlot reservationSlot, Member member);

    @Query("""
            SELECT r
            FROM Reservation r
            JOIN FETCH r.reservationSlot.time t
            JOIN FETCH r.reservationSlot.theme th
            JOIN FETCH r.member m
            WHERE th.id = :themeId
              AND m.id = :memberId
              AND r.reservationSlot.date BETWEEN :dateFrom AND :dateTo
            """
    )
    List<Reservation> findAllByThemeIdAndMemberIdAndDateRange(
            final Long themeId,
            final Long memberId,
            final LocalDate dateFrom,
            final LocalDate dateTo
    );

    @Query("""
            SELECT r
            FROM Reservation r
            WHERE r.reservationSlot.date = :date
            AND r.reservationSlot.theme = :theme
            """
    )
    List<Reservation> findAllByDateAndTheme(LocalDate date, Theme theme);

    List<Reservation> findAllByMember(Member member);
}
