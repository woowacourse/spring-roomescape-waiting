package roomescape.repository.jpa;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import roomescape.domain.member.Member;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationStatus;
import roomescape.domain.reservationitem.ReservationItem;
import roomescape.repository.querydsl.ReservationRepositoryCustom;

@Repository
public interface ReservationJpaRepository extends JpaRepository<Reservation, Long>, ReservationRepositoryCustom {

    @Query("""
        SELECT COUNT(r)
        FROM Reservation r
        WHERE r.reservationItem.id = :reservationItemId
        AND r.id < :currentReservationId
        """)
    long countByReservationItemIdAndIdLessThan(
            @Param("reservationItemId") Long reservationItemId,
            @Param("currentReservationId") Long currentReservationId
    );

    boolean existsByMemberAndReservationItem(Member member, ReservationItem reservationItem);

    Optional<Reservation> findFirstByReservationItemAndReservationStatusOrderByIdAsc(ReservationItem reservationItem, ReservationStatus reservationStatus);

    List<Reservation> findByReservationStatusOrderByIdDesc(ReservationStatus reservationStatus);

    List<Reservation> findByMemberId(Long memberId);
}
