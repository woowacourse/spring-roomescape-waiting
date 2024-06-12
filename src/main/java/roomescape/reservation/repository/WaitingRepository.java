package roomescape.reservation.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import roomescape.member.model.Member;
import roomescape.reservation.model.Reservation;
import roomescape.reservation.model.Waiting;
import roomescape.reservation.dto.WaitingWithRank;

@Repository
public interface WaitingRepository extends JpaRepository<Waiting, Long> {

    @Query("""
                SELECT COUNT(w)
                         FROM Waiting w
                         WHERE w.reservation.id = :#{#waiting.reservation.id}
                           AND w.id < :#{#waiting.id} + 1
            """)
    long countByReservation(Waiting waiting);

    boolean existsByMemberIdAndReservationId(Long memberId, Long reservationId);

    boolean existsByReservationId(Long reservationId);

    @Query("SELECT w.member FROM Waiting w WHERE w.reservation.id = :reservationId ORDER BY w.id ASC LIMIT 1")
    Member findFirstMemberByReservationIdOrderByIdAsc(Long reservationId);

    void deleteByMemberAndReservation(Member firstCandidate, Reservation reservation);

    List<Waiting> findByMemberId(Long memberId);
}
