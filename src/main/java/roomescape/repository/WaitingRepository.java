package roomescape.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;
import roomescape.model.ReservationInfo;
import roomescape.model.Waiting;
import roomescape.model.WaitingWithRank;
import roomescape.model.member.Member;

import java.util.List;
import java.util.Optional;

public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Transactional
    @Query("""
            SELECT new roomescape.model.WaitingWithRank(w1, COUNT(w2) + 1)
            FROM Waiting w1
                LEFT JOIN Waiting w2
                    ON w1.reservationInfo = w2.reservationInfo AND w2.id < w1.id
            WHERE w1.member.id = ?1
            GROUP BY w1
            """)
    List<WaitingWithRank> findWaitingWithRankByMemberId(Long memberId);

    @Transactional
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

    boolean existsByReservationInfo_TimeId(long id);

    boolean existsByReservationInfo_ThemeId(long id);
}
