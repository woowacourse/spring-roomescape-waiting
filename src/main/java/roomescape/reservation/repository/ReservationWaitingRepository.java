package roomescape.reservation.repository;

import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.reservation.domain.ReservationWaiting;
import roomescape.reservation.dto.WaitingWithRank;

public interface ReservationWaitingRepository extends JpaRepository<ReservationWaiting, Long> {

    // TODO 현재 쿼리에서는 select 절이 3개 발사되는 문제가 있다. 자바 내에서 해결하는 것이 더 나을 수도 있다.

    @Query("""
            select new roomescape.reservation.dto.WaitingWithRank(
                w,
                (
                    select count(w2)
                    from ReservationWaiting w2
                    where
                        w2.reservationDatetime.reservationTime = w.reservationDatetime.reservationTime
                        and w2.reservationDatetime.reservationDate.date = w.reservationDatetime.reservationDate.date
                        and w2.id <= w.id
                )
            )
            from ReservationWaiting w
            where w.reserver.id = :memberId
            """)
    List<WaitingWithRank> findWithRankByMemberId(@Param("memberId") Long memberId);


    @Query("""
            select exists
            (select w from ReservationWaiting w
                where w.reserver.id = :memberId
                and w.reservationDatetime.reservationDate.date = :date
                and w.reservationDatetime.reservationTime.id = :timeId
            )
            """)
    boolean existsByMemberIdAndDateAndTimeId(@Param("memberId") Long memberId,
                                             @Param("date") LocalDate date,
                                             @Param("timeId") Long timeId);
}
