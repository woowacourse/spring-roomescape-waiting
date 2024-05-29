package roomescape.infra.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.reservation.Reservation;
import roomescape.domain.reservation.ReservationRepository;
import roomescape.domain.reservation.ReservationWithRank;
import roomescape.domain.reservationdetail.ReservationDetail;
import roomescape.exception.reservation.NotFoundReservationException;

public interface ReservationJpaRepository extends ReservationRepository, Repository<Reservation, Long> {

    @Override
    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    @Override
    @Query(""" 
            select r from Reservation r
            join fetch r.member m
            join fetch r.detail d
            where r.detail.date >= :start
            and r.detail.date <= :end
            and m.id = :memberId
            and d.theme.id = :themeId
            """)
    List<Reservation> findByPeriodAndThemeAndMember(
            @Param("start") LocalDate start, @Param("end") LocalDate end,
            @Param("memberId") Long memberId, @Param("themeId") Long themeId
    );

    @Override
    @Query("""
            select r from Reservation r
            where r.detail = :detail
            and r.status = 'WAITING'
            order by r.createdAt
            limit 1
            """)
    Optional<Reservation> findNextWaitingReservation(@Param("detail") ReservationDetail detail);

    @Override
    @Query(""" 
            select new roomescape.domain.reservation.ReservationWithRank(mine,
                (select count(r) + 1 from Reservation r
                where r.createdAt < mine.createdAt
                and r.detail = mine.detail
                and r.status = roomescape.domain.reservation.Status.WAITING))
            from Reservation mine
            where mine.member.id = :memberId
            and mine.status in (roomescape.domain.reservation.Status.RESERVED,
                                    roomescape.domain.reservation.Status.WAITING)
            and (mine.detail.date > current_date or (mine.detail.date = current_date
                and mine.detail.time.startAt > current_time))
            """)
    List<ReservationWithRank> findWithRank(@Param("memberId") Long memberId);
}
