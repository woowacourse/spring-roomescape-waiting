package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import roomescape.model.ReservationInfo;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.Member;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
            SELECT new roomescape.model.WaitingWithRank(w, (SELECT COUNT(w2) + 1
                        FROM Waiting w2
                        WHERE w2.reservationInfo = w.reservationInfo
                            AND w2.id < w.id))
            FROM Waiting w
            WHERE w.member.id = ?1
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    @Query("""
            SELECT w
            FROM Waiting w
            WHERE w.reservationInfo = ?1
            ORDER BY w.id ASC
            LIMIT 1
            """)
    Optional<Waiting> findFirstByReservationInfo(ReservationInfo reservationInfo);

    boolean existsByReservationInfoAndMember(ReservationInfo reservationInfo, Member member);

    boolean existsByIdAndMemberId(long id, long memberId);
}
