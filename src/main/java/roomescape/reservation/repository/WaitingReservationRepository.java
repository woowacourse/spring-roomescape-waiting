package roomescape.reservation.repository;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import roomescape.member.domain.Member;
import roomescape.reservation.domain.WaitingReservation;
import roomescape.reservation.dto.WaitingReservationWithRank;

public interface WaitingReservationRepository extends JpaRepository<WaitingReservation, Long> {

    @Query("""
            select new roomescape.reservation.dto.WaitingReservationWithRank(
                w.id,
                w.theme.name,
                w.date,
                w.time.startAt,
                (select COUNT(wr)*1L from WaitingReservation wr
                    where wr.theme = w.theme
                      and wr.date = w.date
                      and wr.time = w.time
                      and wr.createdAt <= w.createdAt
                )
            )
            from WaitingReservation w
            where w.member = :member
            """)
    List<WaitingReservationWithRank> findWaitingReservationByMember(@Param("member") Member member);
}
