package roomescape.domain.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.domain.schedule.ReservationDate;

import java.util.Collection;
import java.util.List;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {
    @Query("""
            SELECT r FROM Reservation AS r
            WHERE (:memberId is null or r.member.id = :memberId)
            AND (:themeId is null or r.reservationDetail.theme.id = :themeId)
            AND (:dateFrom is null or r.reservationDetail.schedule.date >= :dateFrom)
            AND (:dateTo is null or r.reservationDetail.schedule.date < :dateTo)""")
    List<Reservation> findBy(@Param("memberId") Long memberId, @Param("themeId") Long themeId,
                             @Param("dateFrom") ReservationDate dateFrom, @Param("dateTo") ReservationDate dateTo);

    List<Reservation> findByMemberId(long memberId);

    boolean existsByReservationDetailIdAndStatusAndMemberId(Long reservationDetailId, ReservationStatus status, Long memberId);

    Collection<Reservation> findByStatusNot(ReservationStatus canceled);
}
