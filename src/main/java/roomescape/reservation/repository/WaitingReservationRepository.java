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
                w.roomEscapeInformation.theme.name,
                w.roomEscapeInformation.date,
                w.roomEscapeInformation.time.startAt,
                (select COUNT(wr)*1L from WaitingReservation wr
                    where wr.roomEscapeInformation.theme = w.roomEscapeInformation.theme
                      and wr.roomEscapeInformation.date = w.roomEscapeInformation.date
                      and wr.roomEscapeInformation.time = w.roomEscapeInformation.time
                      and wr.createdAt <= w.createdAt
                )
            )
            from WaitingReservation w
            where w.member = :member
            """)
    List<WaitingReservationWithRank> findWaitingReservationByMember(@Param("member") Member member);
}
