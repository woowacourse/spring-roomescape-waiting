package roomescape.domain.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import roomescape.domain.Member;
import roomescape.domain.Reservation;
import roomescape.domain.ReservationDetail;
import roomescape.domain.Status;
import roomescape.exception.reservation.NotFoundReservationException;

public interface ReservationRepository extends Repository<Reservation, Long> {
    Reservation save(Reservation reservation);

    default Reservation getById(Long id) {
        return findById(id)
                .orElseThrow(NotFoundReservationException::new);
    }

    Optional<Reservation> findById(Long id);

    List<Reservation> findAll();

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

    @Query(""" 
            select new roomescape.domain.repository.ReservationWithRank(mine,
                (select count(r) from Reservation r
                where r.createdAt < mine.createdAt
                and r.detail = mine.detail
                and r.status in (roomescape.domain.Status.RESERVED, roomescape.domain.Status.WAITING)))
            from Reservation mine
            where mine.member.id = :memberId
            and mine.status in (roomescape.domain.Status.RESERVED, roomescape.domain.Status.WAITING)
            """)
    List<ReservationWithRank> findWithRank(@Param("memberId") Long memberId);

    boolean existsByDetailAndMemberAndStatusNot(ReservationDetail detail, Member member, Status status);

    boolean existsByDetailAndStatus(ReservationDetail reservationDetail, Status status);
}
