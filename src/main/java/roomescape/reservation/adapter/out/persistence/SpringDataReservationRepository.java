package roomescape.reservation.adapter.out.persistence;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.application.port.out.projection.ReservationDetailProjection;
import roomescape.reservation.domain.Reservation;

interface SpringDataReservationRepository extends JpaRepository<Reservation, Long> {
    boolean existsBySlot_Id(long slotId);

    boolean existsByMember_IdAndSlot_Id(long memberId, long slotId);

    @Query("""
            SELECT new roomescape.reservation.application.port.out.projection.ReservationDetailProjection(
                r.id,
                r.member.id,
                r.member.name,
                r.slot.date,
                r.slot.theme.id,
                r.slot.theme.name,
                r.slot.theme.description,
                r.slot.theme.thumbnailUrl,
                r.slot.time.id,
                r.slot.time.startAt
            )
            FROM Reservation r
            ORDER BY r.id
            """)
    List<ReservationDetailProjection> findAllDetails();

    @Query("""
            SELECT r.slot.time.id
            FROM Reservation r
            WHERE r.slot.date = :date
              AND r.slot.theme.id = :themeId
            """)
    List<Long> findTimeIdsByDateAndThemeId(@Param("date") LocalDate date, @Param("themeId") long themeId);

    @Query("""
            SELECT new roomescape.reservation.application.port.out.projection.ReservationDetailProjection(
                r.id,
                r.member.id,
                r.member.name,
                r.slot.date,
                r.slot.theme.id,
                r.slot.theme.name,
                r.slot.theme.description,
                r.slot.theme.thumbnailUrl,
                r.slot.time.id,
                r.slot.time.startAt
            )
            FROM Reservation r
            WHERE r.member.id = :memberId
            ORDER BY r.id
            """)
    List<ReservationDetailProjection> findAllReservationDetailsByMemberId(@Param("memberId") long memberId);
}
